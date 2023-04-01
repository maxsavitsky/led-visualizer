package com.lazydash.audio.visualizer.spectrum.core.model;

import com.lazydash.audio.visualizer.spectrum.system.config.AppConfig;
import javafx.scene.paint.Color;

public class FrequencyBar {
    private double hz;
    private double height;
    private Color color;

    public FrequencyBar(double hz, double height, Color color) {
        this.hz = hz;
        this.height = height;
        double hue = 360 * (1 - height / AppConfig.maxBarHeight);
        //hue = Math.max(0, hue - 75);
        this.color = Color.hsb(hue, 1.0, 1.0);
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        //this.color = color;
    }
}
