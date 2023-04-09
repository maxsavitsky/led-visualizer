package com.lazydash.audio.visualizer.spectrum.core.audio;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.util.LinkedList;
import java.util.List;

public class TarsosCoreAudioEngine {

    private AudioDispatcher dispatcher;

    private List<FFTListener> fttListenerList = new LinkedList<>();

    public List<FFTListener> getFttListenerList() {
        return fttListenerList;
    }

    public void start(TarsosDSPAudioInputStream inputStream, int sampleRate){
        int audioWindowSize = CoreConfig.audioWindowSize;
        int audioWindowNumber = CoreConfig.audioWindowNumber;

        float buffer = sampleRate * (audioWindowSize / 1000f);
        int bufferMax = (int) buffer * audioWindowNumber;
        int bufferOverlap = bufferMax - (int) buffer;

        dispatcher = new AudioDispatcher(inputStream, bufferMax, bufferOverlap);
        //dispatcher.addAudioProcessor(new AudioEngineRestartProcessor(this));
        //dispatcher.addAudioProcessor(new MultichannelToMono(audioFormat.getChannels(), true));
        dispatcher.addAudioProcessor(new FFTAudioProcessor(fttListenerList, sampleRate));

        // run the dispatcher
        dispatcher.run();
    }

    public void start(AudioInputStream stream) {
        AudioFormat audioFormat = stream.getFormat();

        float sampleRate = audioFormat.getSampleRate();
        start(new JVMAudioInputStream(stream), (int) sampleRate);
    }

    public void stop() {
        if (dispatcher != null) {
            dispatcher.stop();
        }
    }

}
