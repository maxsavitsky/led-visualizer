package com.lazydash.audio.visualizer.spectrum.ui.fxml.spectrum.settings;

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

public class BarDecayController {
    public Slider pixelsPerSecondDecay;
    public Slider decayAcceleration;
    public Label decayTimeValue;
    public Label decayAccelerationValue;
    public Spinner<Integer> timeFilter;
    public ComboBox<String> smoothnessType;

    public void initialize() {
        decayTimeValue.setText(String.valueOf(CoreConfig.millisToZero));
        pixelsPerSecondDecay.setValue(CoreConfig.millisToZero);
        pixelsPerSecondDecay.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                CoreConfig.millisToZero = newValue.intValue();
                decayTimeValue.setText(String.valueOf(newValue.intValue()));
            }
        });

        decayAccelerationValue.setText(String.valueOf(CoreConfig.accelerationFactor));
        decayAcceleration.setValue(CoreConfig.accelerationFactor);
        decayAcceleration.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                CoreConfig.accelerationFactor = newValue.intValue();
                decayAccelerationValue.setText(String.valueOf(CoreConfig.accelerationFactor));
            }
        });

        timeFilter.getValueFactory().setValue(CoreConfig.timeFilterSize);
        timeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.timeFilterSize = newValue;
        });

        List<String> smoothnessTypeList = Arrays.asList("SMA", "WMA", "EMA");
        smoothnessType.getItems().addAll(smoothnessTypeList);
        smoothnessType.setValue(CoreConfig.smoothnessType);
        smoothnessType.valueProperty().addListener((observable, oldValue, newValue) -> {
            CoreConfig.smoothnessType = newValue;
        });

    }

    public void updateSmoothnessType(ActionEvent actionEvent) {
        CoreConfig.smoothnessType = smoothnessType.getValue();
    }
}
