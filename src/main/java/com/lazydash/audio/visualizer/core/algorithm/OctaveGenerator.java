package com.lazydash.audio.visualizer.core.algorithm;

import java.util.*;

public class OctaveGenerator {
    private static Map<OctaveSettings, List<Double>> cache = new HashMap<>();

    public static List<Double> getOctaveFrequencies(double centerFrequency, double band, double lowerLimit, double upperLimit) {
        OctaveSettings octaveSettings = new OctaveSettings(centerFrequency, band, lowerLimit, upperLimit);
        List<Double> doubles = cache.get(octaveSettings);
        if (doubles == null) {
            Set<Double> octave = new TreeSet<>();

            addLow(octave, centerFrequency, band, lowerLimit);
            addHigh(octave, centerFrequency, band, upperLimit);

            ArrayList<Double> octaves = new ArrayList<>(octave);
            cache.put(octaveSettings, octaves);

            return octaves;

        } else {
            return doubles;

        }
    }

    private static void addLow(Set<Double> octave, double center, double band, double lowerLimit){
        if (center < lowerLimit) {
            return;
        }

        octave.add(center);

        double fl = center / (Math.pow(2, ( 1d / (2*band) )));
        addLow(octave, fl, band, lowerLimit);
    }

    private static void addHigh(Set<Double> octave, double center, double band, double upperLimit){
        if (center > upperLimit) {
            return;
        }

        octave.add(center);

        double fh = center * (Math.pow(2, ( 1d / (2*band) )));
        addHigh(octave, fh, band, upperLimit);
    }

    private static class OctaveSettings {
        private double centerFrequency;
        private double band;
        private double lowerLimit;
        private double upperLimit;

        OctaveSettings(double centerFrequency, double band, double lowerLimit, double upperLimit) {
            this.centerFrequency = centerFrequency;
            this.band = band;
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OctaveSettings that = (OctaveSettings) o;
            return Double.compare(that.centerFrequency, centerFrequency) == 0 &&
                    Double.compare(that.band, band) == 0 &&
                    Double.compare(that.lowerLimit, lowerLimit) == 0 &&
                    Double.compare(that.upperLimit, upperLimit) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(centerFrequency, band, lowerLimit, upperLimit);
        }
    }

}
