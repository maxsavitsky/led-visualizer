package com.lazydash.audio.visualizer.spectrum.core.algorithm;

import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import com.lazydash.audio.visualizer.spectrum.core.model.FrequencyBar;
import javafx.scene.paint.Color;

public class FrequencyBarsColorCalculator {

    private static final long CHANGE_INTERVAL_MILLIS = 500;
    private static final Color DEFAULT_COLOR = Color.hsb(189, 1, 1);

    private Color startColor = DEFAULT_COLOR;
    private Color endColor = DEFAULT_COLOR;

    private long startTime = System.currentTimeMillis();

    public void setNextColor(Color color) {
        startColor = getCurrentColor();
        endColor = color;
        startTime = System.currentTimeMillis();
    }

    public Color getCurrentColor() {
        long interval = System.currentTimeMillis() - startTime;
        if (interval >= CHANGE_INTERVAL_MILLIS) {
            return endColor;
        }
        double fraction = (double) interval / CHANGE_INTERVAL_MILLIS;
        double red = interpolate(startColor.getRed(), endColor.getRed(), fraction);
        double green = interpolate(startColor.getGreen(), endColor.getGreen(), fraction);
        double blue = interpolate(startColor.getBlue(), endColor.getBlue(), fraction);
        return Color.color(red, green, blue);
    }

    private double interpolate(double startValue, double endValue, double fraction) {
        return startValue + fraction * (endValue - startValue);
    }

    public Color getColorForFrequencyBar(FrequencyBar frequencyBar) {
        double heightRatio = frequencyBar.getHeightRatio();
        if (CoreConfig.barsColorMode == 0) {
            return Color.hsb(360.0 * (1.0 - heightRatio), 1, 1);
        }
        Color cur = getCurrentColor();
        double opacityThreshold = 0.4;
        //double opacity = Math.min(1.0, heightRatio / opacityThreshold);
        double opacity = heightRatio;
        return Color.hsb(cur.getHue(), 1.0, 1.0, opacity);
    }

}
