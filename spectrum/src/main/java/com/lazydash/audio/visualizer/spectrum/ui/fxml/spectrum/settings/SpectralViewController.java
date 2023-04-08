package com.lazydash.audio.visualizer.spectrum.ui.fxml.spectrum.settings;

import com.lazydash.audio.visualizer.spectrum.core.algorithm.AmplitudeWeightCalculator;
import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpectralViewController {
    public Slider signalAmplification;
    public Slider signalThreshold;
    public Label signalAmplificationValue;
    public Label signalThresholdValue;
    public Spinner<Integer> frequencyStart;
    public Spinner<Integer> frequencyEnd;
    public Spinner<Integer> octave;
    public Spinner<Integer> frequencyCenter;
    public ComboBox<String> maxLevel;
    public ComboBox<String> weighting;
    public Spinner<Integer> minBarHeight;
    public Spinner<Integer> barGap;

    public void initialize() {
        signalAmplificationValue.setText(String.valueOf(CoreConfig.signalAmplification));
        signalAmplification.setValue(CoreConfig.signalAmplification);
        signalAmplification.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                CoreConfig.signalAmplification = newValue.intValue();
                signalAmplificationValue.setText(String.valueOf(CoreConfig.signalAmplification));
            }
        });

        signalThresholdValue.setText(String.valueOf(CoreConfig.signalThreshold));
        signalThreshold.setValue(CoreConfig.signalThreshold);
        signalThreshold.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                CoreConfig.signalThreshold = newValue.intValue();
                signalThresholdValue.setText(String.valueOf(CoreConfig.signalThreshold));
            }
        });

        frequencyStart.getValueFactory().setValue(CoreConfig.frequencyStart);
        frequencyStart.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.frequencyStart = newValue;
        });

        frequencyCenter.getValueFactory().setValue(CoreConfig.frequencyCenter);
        frequencyCenter.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.frequencyCenter = newValue;
        });

        frequencyEnd.getValueFactory().setValue(CoreConfig.frequencyEnd);
        frequencyEnd.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.frequencyEnd = newValue;
        });

        octave.getValueFactory().setValue(CoreConfig.octave);
        octave.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.octave = newValue;
        });

        maxLevel.setValue(CoreConfig.maxLevel);
        maxLevel.getItems().addAll(Arrays.asList("RMS", "Peak"));

        weighting.setValue(CoreConfig.weight);

        AmplitudeWeightCalculator.WeightWindow[] weightWindows = AmplitudeWeightCalculator.WeightWindow.values();
        List<String> collect = Arrays.stream(weightWindows).map(Enum::toString).collect(Collectors.toList());
        weighting.getItems().addAll(collect);

        minBarHeight.getValueFactory().setValue(CoreConfig.minBarHeight);
        minBarHeight.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.minBarHeight = newValue;
        });

        barGap.getValueFactory().setValue(CoreConfig.barGap);
        barGap.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.barGap = newValue;
        });
    }


    public void updateMaxLeve(ActionEvent actionEvent) {
        CoreConfig.maxLevel = maxLevel.getValue();
    }

    public void updateWeighting(ActionEvent actionEvent) {
        CoreConfig.weight = weighting.getValue();
    }
}
