package com.lazydash.audio.visualizer.spectrum.ui.code.spectral;

import com.lazydash.audio.visualizer.spectrum.core.service.FrequencyBarsFFTService;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpectralAnimator {

    public static final int FPS = 50;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpectralAnimator.class);
    private long oldTime = System.currentTimeMillis();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private FrequencyBarsFFTService spectralFFTService;
    private SpectralView spectralView;

    public SpectralAnimator(FrequencyBarsFFTService spectralFFTService, SpectralView spectralView) {
        this.spectralFFTService = spectralFFTService;
        this.spectralView = spectralView;
    }

    public void play(){
        scheduledExecutorService.scheduleAtFixedRate(()-> Platform.runLater(this::updateSpectralView), 1000, 1000 / FPS, TimeUnit.MILLISECONDS);
    }

    public void stop(){
        scheduledExecutorService.shutdownNow();
    }

    private void updateSpectralView(){
        spectralView.updateState(spectralFFTService.getFrequencyBarList());

        long newTime = System.currentTimeMillis();
        long deltaTime = newTime - oldTime;

//        LOGGER.info(String.valueOf(deltaTime));
        oldTime = newTime;
    }

}
