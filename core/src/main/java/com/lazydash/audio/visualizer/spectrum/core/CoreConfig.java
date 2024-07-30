package com.lazydash.audio.visualizer.spectrum.core;

public class CoreConfig {
    // Audio Input
    public static int audioWindowSize = 30;
    public static int audioWindowNumber = 3;

    // Spectral View
    public static int signalAmplification = 120;
    public static int signalThreshold = -34;
    public static String maxLevel = "RMS";
    public static String weight = "dBB";
    public static int frequencyStart = 39;
    public static int frequencyCenter = 8000;
    public static int frequencyEnd = 16001;
    public static int octave = 18;
    public static int minBarHeight = 2;
    public static int barGap = 1;

    public static double maxBarHeight = 352;
    public static int hzLabelHeight = 20;

    public static int barsColorMode = 0;


    // bar acceleration
    public static int millisToZero = 400;
    public static int accelerationFactor = 4;
    public static int timeFilterSize = 2;
    public static String smoothnessType = "WMA";

    // window
    public static double windowWidth = 1700;
    public static double windowHeight = 400;

    public static final int MAX_LED_COUNT = 284;
    public static int ledCount = 284;
}
