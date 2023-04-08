package com.lazydash.audio.visualizer.spectrum.core.model;

import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;

public class FrequencyBar {
    private double hz;
    private double height;
    private double colorHue;

    public FrequencyBar(double hz, double height) {
        this.hz = hz;
        this.height = height;
        this.colorHue = 360 * (1 - height / CoreConfig.maxBarHeight);
    }

    public double getHz() {
        return hz;
    }

    public void setHz(double hz) {
        this.hz = hz;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getColorHue() {
        return colorHue;
    }
}
