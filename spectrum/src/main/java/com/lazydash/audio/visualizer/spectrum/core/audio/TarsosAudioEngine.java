package com.lazydash.audio.visualizer.spectrum.core.audio;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import com.lazydash.audio.visualizer.spectrum.system.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class TarsosAudioEngine {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private AudioDispatcher dispatcher;
    private Thread audioThread;

    private List<FFTListener> fttListenerList = new LinkedList<>();

    public List<FFTListener> getFttListenerList() {
        return fttListenerList;
    }

    public void start() {
        AudioFormat audioFormat = getAudioFormat();

        float sampleRate = audioFormat.getSampleRate();
        int audioWindowSize = AppConfig.audioWindowSize;
        int audioWindowNumber = AppConfig.audioWindowNumber;

        float buffer = sampleRate * (audioWindowSize / 1000f);
        int bufferMax = (int) buffer * audioWindowNumber;
        int bufferOverlap = bufferMax - (int) buffer;

        /*TargetDataLine line = getLine(audioFormat, bufferMax);
        run(line, audioFormat, bufferMax, bufferOverlap);*/

        audioThread = new Thread(()->{
            try (ServerSocket serverSocket = new ServerSocket(13485)) {
                while(!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    socket.setKeepAlive(true);
                    try (InputStream is = socket.getInputStream();
                         NetworkInputStream nis = new NetworkInputStream(is);
                         AudioInputStream stream = new AudioInputStream(nis, audioFormat, AudioSystem.NOT_SPECIFIED)) {
                        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);

                        dispatcher = new AudioDispatcher(audioStream, bufferMax, bufferOverlap);
                        //dispatcher.addAudioProcessor(new AudioEngineRestartProcessor(this));
                        //dispatcher.addAudioProcessor(new MultichannelToMono(audioFormat.getChannels(), true));
                        dispatcher.addAudioProcessor(new FFTAudioProcessor(audioFormat, fttListenerList));

                        // run the dispatcher (on a new thread).
                        dispatcher.run();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "Audio dispatching");
        audioThread.setDaemon(true);
        audioThread.start();

    }

    public void stop() {
        try {
            if (dispatcher != null) {
                audioThread.interrupt();
                dispatcher.stop();

                // wait 5 seconds for audio dispatcher to finish
                audioThread.join(1 * 1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void restart() {
        stop();
        start();
    }

    private TargetDataLine getLine(AudioFormat audioFormat, int lineBuffer) throws LineUnavailableException {
        TargetDataLine targetDataLine;
        try {
                targetDataLine = getLineFromConfig(audioFormat, lineBuffer);

        } catch (Exception e) {
            LOGGER.info("Unable to get the audio line from config file");
            targetDataLine = getFirstLineAvailable(audioFormat, lineBuffer);
        }

        return targetDataLine;
    }

    private TargetDataLine getLineFromConfig(AudioFormat audioFormat, int lineBuffer) throws LineUnavailableException {
        //noinspection OptionalGetWithoutIsPresent
        Mixer.Info mixerInfo = Stream.of(AudioSystem.getMixerInfo())
                .filter(curentMixerInfo -> curentMixerInfo.getName().equals(AppConfig.inputDevice))
                .findFirst()
                .get();
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        LOGGER.info("mixer info: " + mixerInfo);

        //noinspection OptionalGetWithoutIsPresent
        Line.Info lineInfo = Stream.of(mixer.getTargetLineInfo()).findFirst().get();
        TargetDataLine line = (TargetDataLine) mixer.getLine(lineInfo);
        line.open(audioFormat, lineBuffer);
        line.start();

        LOGGER.info("line format: " + line.getFormat());
        LOGGER.info("line info: " + line.getLineInfo());
        LOGGER.info("line bufferSize size: " + line.getBufferSize());

        return line;
    }

    private TargetDataLine getFirstLineAvailable(AudioFormat audioFormat, int lineBuffer) throws LineUnavailableException {
        TargetDataLine line;

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            for (Line.Info mixerLineInfo : mixer.getTargetLineInfo()) {
                try {
                    // try to get access to the mixer line
                    // if successful ad the mixer name to the list
                    line = (TargetDataLine) mixer.getLine(mixerLineInfo);
                    line.open(audioFormat, lineBuffer);
                    line.start();

                    AppConfig.inputDevice = mixerInfo.getName();

                    LOGGER.info("mixer info: " + mixerInfo);
                    LOGGER.info("line format: " + line.getFormat());
                    LOGGER.info("line info: " + line.getLineInfo());
                    LOGGER.info("line bufferSize size: " + line.getBufferSize());

                    return line;

                } catch (Exception e) {
                    // skip mixer if line can not be obtained
                }
            }
        }

        throw new LineUnavailableException("No available input line found");
    }

    private AudioFormat getAudioFormat() {
        /*return new AudioFormat(
                AppConfig.sampleRate,
                AppConfig.sampleSizeInBits,
                AppConfig.channels,
                AppConfig.signed,
                AppConfig.bigEndian
        );*/
        return new AudioFormat(48000, 16, 1, true, false);
    }

    private void run(TargetDataLine line, AudioFormat audioFormat, int bufferSize, int bufferOverlay) {
        final AudioInputStream stream = new AudioInputStream(line);
        run(stream, audioFormat, bufferSize, bufferOverlay);
    }

    private void run(AudioInputStream stream, AudioFormat audioFormat, int bufferSize, int bufferOverlay){
        JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);

        dispatcher = new AudioDispatcher(audioStream, bufferSize, bufferOverlay);
//        dispatcher.addAudioProcessor(new AudioEngineRestartProcessor(this));
//        dispatcher.addAudioProcessor(new MultichannelToMono(audioFormat.getChannels(), true));
        dispatcher.addAudioProcessor(new FFTAudioProcessor(audioFormat, fttListenerList));

        // run the dispatcher (on a new thread).
        audioThread = new Thread(dispatcher, "Audio dispatching");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    private static class NetworkInputStream extends InputStream {

        private final static int FIRST_PACKET_SIZE = 60;
        private final static int BYTES_TO_SKIP_COUNT = 36;

        private final byte[] buffer = new byte[4096];
        private int bufferOffset = 0;
        private int bufferLength = 0;

        private final InputStream is;

        public NetworkInputStream(InputStream is) {
            this.is = is;
        }

        private boolean fillBuffer() throws IOException {
            int len = is.read(buffer);
            if(len == FIRST_PACKET_SIZE) // skip
                return fillBuffer();
            if(len == -1)
                return false;
            if(len < BYTES_TO_SKIP_COUNT){
                bufferOffset = bufferLength = len;
            }else{
                bufferLength = len;
                bufferOffset = BYTES_TO_SKIP_COUNT;
            }
            return true;
        }

        @Override
        public int read() throws IOException {
            if(bufferOffset == bufferLength){
                if(!fillBuffer())
                    return -1;
            }
            return buffer[bufferOffset++] & 0xFF;
        }
    }

}
