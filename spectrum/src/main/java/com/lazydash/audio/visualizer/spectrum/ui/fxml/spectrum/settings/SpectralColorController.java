package com.lazydash.audio.visualizer.spectrum.ui.fxml.spectrum.settings;

import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public class SpectralColorController {

    public ComboBox<String> barsColor;
    public CheckBox showWindowCheckBox;

    public void initialize() {
        barsColor.getItems().addAll("bars' height", "music mood");
        barsColor.getSelectionModel().select(CoreConfig.barsColorMode);

        showWindowCheckBox.setSelected(CoreConfig.showColorMapWindow);
    }

    public void updateBarsColor(ActionEvent actionEvent) {
        CoreConfig.barsColorMode = barsColor.getSelectionModel().getSelectedIndex();
    }

    public void updateShowColorMap(ActionEvent actionEvent) {
        CoreConfig.showColorMapWindow = showWindowCheckBox.isSelected();
    }

}
