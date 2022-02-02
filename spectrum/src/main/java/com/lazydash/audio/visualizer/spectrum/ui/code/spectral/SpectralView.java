package com.lazydash.audio.visualizer.spectrum.ui.code.spectral;

import com.lazydash.audio.visualizer.spectrum.core.model.FrequencyBar;
import com.lazydash.audio.visualizer.spectrum.system.config.AppConfig;
import com.lazydash.audio.visualizer.spectrum.system.config.WindowProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpectralView extends GridPane {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpectralView.class);
    private List<FrequencyView> frequencyViewList = new ArrayList<>();

    public void configure() {
        this.setAlignment(Pos.BOTTOM_CENTER);

        RowConstraints rowConstraintsAmplitudes = new RowConstraints();
        rowConstraintsAmplitudes.setVgrow(Priority.NEVER);
        this.getRowConstraints().add(rowConstraintsAmplitudes);

        RowConstraints rowConstraintsHz = new RowConstraints();
        rowConstraintsHz.setMinHeight(AppConfig.hzLabelHeight);
        this.getRowConstraints().add(rowConstraintsHz);



        WindowProperty.heightProperty.addListener((observable, oldValue, newValue) -> {
            AppConfig.maxBarHeight = newValue.intValue() - AppConfig.hzLabelHeight;
        });

        WindowProperty.widthProperty.addListener((observable, oldValue, newValue) -> {
            this.setPrefWidth(newValue.doubleValue());
            frequencyViewList.forEach(frequencyView -> {
                int rectangleWidth = (int) (this.getWidth() / frequencyViewList.size()) - AppConfig.barGap;
                frequencyView.getRectangle().setWidth(rectangleWidth);
            });
        });
    }

    public void updateState(List<FrequencyBar> frequencyBarList) {
        this.setHgap(AppConfig.barGap);

        if (frequencyBarList.size() != frequencyViewList.size()) {
            createBars(frequencyBarList);
            updateBars(frequencyBarList);
//            LOGGER.info("create bars");

        } else {
            updateBars(frequencyBarList);
//            LOGGER.info("update bars");
        }
    }

    private void updateBars(List<FrequencyBar> frequencyBarList) {
        for (int i = 0; i < frequencyViewList.size(); i++) {
            FrequencyView frequencyView = frequencyViewList.get(i);
            FrequencyBar frequencyBar = frequencyBarList.get(i);

            frequencyView.setHzValue(frequencyBar.getHz());

            frequencyView.getRectangle().setFill(frequencyBar.getColor());
            // rounding is needed because of the subpixel rendering
            frequencyView.getRectangle().setHeight(Math.round(frequencyBar.getHeight()));

            int rectangleWidth = (int) (this.getWidth() / frequencyViewList.size()) - AppConfig.barGap;
            frequencyView.getRectangle().setWidth(rectangleWidth);
        }
    }

    private void createBars(List<FrequencyBar> frequencyBarList) {
        frequencyViewList.clear();
        this.getChildren().clear();

        for (int i = 0; i < frequencyBarList.size(); i++) {
            FrequencyView frequencyView = new FrequencyView();
            frequencyViewList.add(frequencyView);

            this.add(frequencyView.getRectangle(), i, 0);
            this.add(frequencyView.getHzLabel(), i, 1);

            GridPane.setValignment(frequencyView.getRectangle(), VPos.BOTTOM);
            GridPane.setHalignment(frequencyView.getHzLabel(), HPos.CENTER);
        }
    }
}
