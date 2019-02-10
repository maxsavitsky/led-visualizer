package com.lazydash.audio.visualizer.core.algorithm;

import com.lazydash.audio.visualizer.system.config.AppConfig;

public class BarsHeightCalculator {
    private float[] amplitudes;
    private double[] decay;

    // holds state and modifies it's internal state based on the input
    public float[] processAmplitudes(float[] newAmplitudes, double targetFPS) {
        // init on first run or if number of newAmplitudes has changed
        if (amplitudes == null || amplitudes.length != newAmplitudes.length) {
            amplitudes = new float[newAmplitudes.length];
            System.arraycopy(newAmplitudes, 0, amplitudes, 0, newAmplitudes.length);

            decay = new double[amplitudes.length];
        }

        for (int i = 0; i < newAmplitudes.length; i++) {
            double oldHeight = amplitudes[i];

            double maxHeight = Math.abs(AppConfig.getSignalThreshold());
            double newHeight = (newAmplitudes[i] + Math.abs(AppConfig.getSignalThreshold()));
//            double newHeight = (newAmplitudes[i]);
            double windowHeight = AppConfig.getMaxBarHeight();

            newHeight =(((windowHeight) / maxHeight)) * (newHeight * (AppConfig.getSignalAmplification() / 100d));
//            newHeight = (newHeight * (AppConfig.getSignalAmplification()));

            // apply limits
            if (newHeight > AppConfig.getMaxBarHeight()) {
                // ceiling hit
                newHeight = AppConfig.getMaxBarHeight();

            } else if (newHeight < AppConfig.getMinBarHeight()) {
                // below floor
                newHeight = AppConfig.getMinBarHeight();
            }


            if (newHeight > oldHeight) {
                // use new height
                amplitudes[i] = (float) newHeight;
                decay[i] = 0;

            } else if (newHeight < oldHeight) {
                // decayFrames is per bar because of acceleration variances
                double decayFrames = 0;
                if (AppConfig.getDecayTime() > 0) {
                    decayFrames = (AppConfig.getMaxBarHeight() * (1000d / targetFPS) / AppConfig.getDecayTime());
                }

                if (AppConfig.getAccelerationFactor() > 0 && decay[i] < 1) {
                    double accelerationStep = (decayFrames / (decayFrames * AppConfig.getAccelerationFactor()));
                    decay[i] = decay[i] + accelerationStep;
                    decayFrames = decayFrames * decay[i];
                }

                if (oldHeight - decayFrames < newHeight) {
                    decay[i] = 0;
                    amplitudes[i] = (float) newHeight;

                } else {
                    amplitudes[i] = (float) (amplitudes[i] - decayFrames);
                }

            }

            // correction for bellow min bar height
            if (amplitudes[i] < AppConfig.getMinBarHeight()) {
                amplitudes[i] = AppConfig.getMinBarHeight();
            }

        }

        return amplitudes;
    }
}
