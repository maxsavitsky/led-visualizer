package com.lazydash.audio.visualizer.spectrum.core.algorithm;

import com.lazydash.audio.visualizer.spectrum.core.model.FrequencyBar;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class FrequencyBarsCreator {

    public static List<FrequencyBar> createFrequencyBars(double[] binsHz, double[] amplitudes, FrequencyBarsColorCalculator colorCalculator) {
        List<FrequencyBar> frequencyBars = new ArrayList<>(binsHz.length);
        for (int i = 0; i < binsHz.length; i++) {
            FrequencyBar bar = new FrequencyBar(binsHz[i], amplitudes[i]);
            bar.setColor(colorCalculator.getColorForFrequencyBar(bar));
            frequencyBars.add(bar);
        }

        return frequencyBars;
    }
}
