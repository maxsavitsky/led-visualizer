package com.lazydash.audio.visualizer.spectrum.core.model;

import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import javafx.scene.paint.Color;

public class FrequencyBar {
    private double hz;
    private double height;
    private Color color;

    public FrequencyBar(double hz, double height) {
        this.hz = hz;
        this.height = height;
        //this.colorHue = 360 * (1 - height / CoreConfig.maxBarHeight);
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
        return color.getHue();
    }

    public double getColorBrightness() {
        return color.getBrightness();
    }

    public double getColorSaturation() {
        return color.getSaturation();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public double getHeightRatio() {
        return Math.max(0, Math.min(height / CoreConfig.maxBarHeight, 1.0));
    }
}
