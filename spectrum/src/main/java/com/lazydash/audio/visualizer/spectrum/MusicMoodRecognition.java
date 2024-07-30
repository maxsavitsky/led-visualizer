package com.lazydash.audio.visualizer.spectrum;

import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import com.lazydash.audio.visualizer.spectrum.core.TarsosAudioEngine;
import com.lazydash.audio.visualizer.spectrum.core.algorithm.FrequencyBarsColorCalculator;
import javafx.scene.paint.Color;
import org.apache.commons.io.input.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py4j.GatewayServer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;

public class MusicMoodRecognition {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicMoodRecognition.class);

    private final FrequencyBarsColorCalculator barsColorCalculator;
    private final TarsosAudioEngine tarsosAudioEngine;
    private final PipedInputStream pipedInputStream = new PipedInputStream(1024 * 1024);

    public MusicMoodRecognition(FrequencyBarsColorCalculator barsColorCalculator, TarsosAudioEngine tarsosAudioEngine) {
        this.barsColorCalculator = barsColorCalculator;
        this.tarsosAudioEngine = tarsosAudioEngine;

        tarsosAudioEngine.setSplittedInputStream(pipedInputStream);
    }

    public PipedInputStream getPipedInputStream() {
        return pipedInputStream;
    }

    public void newColorDetected(int red, int green, int blue) {
        LOGGER.info("rgb received: {}, {}, {}", red, green, blue);
        barsColorCalculator.setNextColor(Color.rgb(red, green, blue));
    }

    public void log(String message) {
        LOGGER.info(message);
    }

    public Reader getReader() {
        AudioInputStream sourceStream = new AudioInputStream(pipedInputStream, TarsosAudioEngine.getAudioFormat(), AudioSystem.NOT_SPECIFIED);
        AudioFormat audioFormat = new AudioFormat(8000, 16, 1, true, false);
        var stream = AudioSystem.getAudioInputStream(audioFormat, sourceStream);
        return new Reader(stream);
    }

    public boolean showWindow() {
        return CoreConfig.barsColorMode == 1 && CoreConfig.showColorMapWindow;
    }

    public void start() {
        new Thread(this::run).start();
    }

    private void run() {
        GatewayServer server = new GatewayServer(this);
        server.start();

        LOGGER.info("Waiting for stream...");
        try {
            tarsosAudioEngine.getCountDownLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Starting python process");

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File("color_your_music_mood"));
        builder.command("c:\\Program Files\\Python\\Python368\\python.exe", "main.py");
        try {
            builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Reader {

        private final AudioInputStream stream;

        public Reader(AudioInputStream stream) {
            this.stream = stream;
        }

        public byte[] read(int frames) throws IOException {
            int bytesCount = frames * stream.getFormat().getFrameSize();
            byte[] bytes = new byte[bytesCount];
            stream.read(bytes);
            return bytes;
        }

    }

}
