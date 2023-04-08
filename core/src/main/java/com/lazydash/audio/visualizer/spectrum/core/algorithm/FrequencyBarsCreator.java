package com.lazydash.audio.visualizer.spectrum.core.algorithm;

import com.lazydash.audio.visualizer.spectrum.core.model.FrequencyBar;

import java.util.ArrayList;
import java.util.List;

public class FrequencyBarsCreator {

    public static List<FrequencyBar> createFrequencyBars(double[] binsHz, double[] amplitudes) {
        List<FrequencyBar> frequencyBars = new ArrayList<>(binsHz.length);
        for (int i = 0; i < binsHz.length; i++) {
            frequencyBars.add(new FrequencyBar(binsHz[i], amplitudes[i]));
        }

        return frequencyBars;
    }
}
