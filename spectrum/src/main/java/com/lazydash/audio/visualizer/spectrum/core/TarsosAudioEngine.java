package com.lazydash.audio.visualizer.spectrum.core;

import com.lazydash.audio.visualizer.spectrum.core.audio.FFTListener;
import com.lazydash.audio.visualizer.spectrum.core.audio.TarsosCoreAudioEngine;
import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TarsosAudioEngine {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final TarsosCoreAudioEngine coreAudioEngine = new TarsosCoreAudioEngine();

    private ServerSocket serverSocket;
    private Socket currentSocket;
    private Thread audioThread;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private PipedInputStream splittedInputStream = null;

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setSplittedInputStream(PipedInputStream splittedInputStream) {
        this.splittedInputStream = splittedInputStream;
    }

    public void start(boolean isNativeReceive){
        audioThread = new Thread(()->{
            try {
                serverSocket = new ServerSocket(isNativeReceive ? 13486 : 13485);
                while(!serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    LOGGER.info("waiting for socket " + serverSocket.isClosed() );
                    currentSocket = serverSocket.accept();
                    currentSocket.setKeepAlive(true);
                    try (InputStream is = currentSocket.getInputStream()) {
                        AudioInputStream audioStream = getAudioInputStream(isNativeReceive, is);
                        countDownLatch.countDown();
                        coreAudioEngine.start(audioStream);
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

    private AudioInputStream getAudioInputStream(boolean isNativeReceive, InputStream is) throws IOException {
        InputStream sourceInputStream = isNativeReceive ? is : new NetworkInputStream(is);
        InputStream inputStream;
        if (splittedInputStream == null) {
            inputStream = sourceInputStream;
        } else {
            inputStream = new TeeInputStream(sourceInputStream, new PipedOutputStream(splittedInputStream));
        }
        return new AudioInputStream(inputStream, getAudioFormat(), AudioSystem.NOT_SPECIFIED);
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

    public static AudioFormat getAudioFormat() {
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
