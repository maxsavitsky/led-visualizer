package com.lazydash.audio.visualizer.spectrum.core;

import com.lazydash.audio.visualizer.spectrum.core.audio.FFTListener;
import com.lazydash.audio.visualizer.spectrum.core.audio.TarsosCoreAudioEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class TarsosAudioEngine {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final TarsosCoreAudioEngine coreAudioEngine = new TarsosCoreAudioEngine();

    private ServerSocket serverSocket;
    private Socket currentSocket;
    private Thread audioThread;

    public void start(boolean isNativeReceive){
        AudioFormat audioFormat = getAudioFormat();
        audioThread = new Thread(()->{
            try {
                serverSocket = new ServerSocket(isNativeReceive ? 13486 : 13485);
                while(!serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    LOGGER.info("waiting for socket " + serverSocket.isClosed() );
                    currentSocket = serverSocket.accept();
                    currentSocket.setKeepAlive(true);
                    try (InputStream is = currentSocket.getInputStream();
                         InputStream audioStream = isNativeReceive ? is : new NetworkInputStream(is);
                         AudioInputStream stream = new AudioInputStream(audioStream, audioFormat, AudioSystem.NOT_SPECIFIED)) {
                        coreAudioEngine.start(stream);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("", e);
                throw new RuntimeException(e);
            }
            LOGGER.info("Audio thread stopping");
        }, "Audio dispatching");
        audioThread.setDaemon(true);
        audioThread.start();
    }

    public void stop() {
        try {
            coreAudioEngine.stop();
            try {
                if (serverSocket != null)
                    serverSocket.close();
                if(currentSocket != null)
                    currentSocket.close();
            }catch (IOException e){
                LOGGER.error("failed to close server socket", e);
            }

            audioThread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        LOGGER.info(String.valueOf(audioThread.getState()));
    }

    public List<FFTListener> getFttListenerList(){
        return coreAudioEngine.getFttListenerList();
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(48000, 16, 1, true, false);
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
