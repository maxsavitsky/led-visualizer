package com.lazydash.audio.visualiser.system.config;

import com.lazydash.audio.visualiser.core.model.ColorBand;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

public class ColorConfig {
    public static Color baseColor = Color.color(0.01, 0.005, 0.01);

    public static List<ColorBand> colorBands = Arrays.asList(
            new ColorBand(
                    Color.color(0, 0, 1, 1),
                    Color.color(0.5, 0.0, 0.5, 1),
                    Integer.MIN_VALUE,
                    80),

            new ColorBand(
                    Color.color(0.5, 0, 0.5, 1),
                    Color.color(1.0, 0.0, 0.0, 1),
                    81,
                    Integer.MAX_VALUE)
    );
}