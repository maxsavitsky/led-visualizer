package com.lazydash.audio.visualizer.spectrum;

import com.lazydash.audio.visualizer.spectrum.core.TarsosAudioEngine;
import com.lazydash.audio.visualizer.spectrum.core.service.FrequencyBarsFFTService;
import com.lazydash.audio.visualizer.spectrum.plugin.PluginSystem;
import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import com.lazydash.audio.visualizer.spectrum.system.config.WindowProperty;
import com.lazydash.audio.visualizer.spectrum.system.persistance.ConfigFilePersistence;
import com.lazydash.audio.visualizer.spectrum.ui.code.spectral.SpectralAnimator;
import com.lazydash.audio.visualizer.spectrum.ui.code.spectral.SpectralView;
import com.maxsavitsky.visualizer.datasender.LedDataSenderService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/*
Intellij settings:
    - vm options: -Djava.library.path=./dll --module-path "/pathToJavaFxHome/lib" --add-modules=javafx.controls,javafx.fxml
    - working directory: $MODULE_WORKING_DIR$
*/
public class Main extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            launch(args);

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        // load all the native apis
        /*Path dllPath = Paths.get("./dll");
        if (Files.exists(dllPath)) {
            String[] list = dllPath.toFile().list();
            if (list != null) {
                Arrays.stream(list).forEach(fileName -> {
                    String extension = fileName.substring(fileName.lastIndexOf('.'));
                    if (extension.equals(".dll")) {
                        System.loadLibrary(fileName.substring(0, fileName.lastIndexOf('.')));
                    }
                });
            }
        }*/

        // plugins
        PluginSystem.getInstance().startAllPlugins();

        // create
        ConfigFilePersistence configFilePersistence = new ConfigFilePersistence();
        configFilePersistence.load(CoreConfig.class, "./application.properties");
        TarsosAudioEngine tarsosAudioEngine = new TarsosAudioEngine();

        SpectralView spectralView = new SpectralView();
        Scene scene = createScene(spectralView);
        Stage settingsStage = createSettingsStage();

        FrequencyBarsFFTService spectralFFTService = new FrequencyBarsFFTService();
        SpectralAnimator spectralAnimator = new SpectralAnimator(spectralFFTService, spectralView);

        LedDataSenderService dataSenderService = new LedDataSenderService(spectralFFTService);

        // setup
        spectralView.configure();
        configureStage(stage, scene);

        // wire
        tarsosAudioEngine.getFttListenerList().add(spectralFFTService);
        PluginSystem.getInstance().registerAllFffPlugins(tarsosAudioEngine);

        wireSettingsStage(settingsStage, scene);
        wirePrimaryStage(stage, configFilePersistence, tarsosAudioEngine, dataSenderService, spectralAnimator);

        // run
        stage.show();
        spectralAnimator.play();
        tarsosAudioEngine.start();
        dataSenderService.start();
    }

    private void wireSettingsStage(Stage settingsStage, Scene rootScene) {
        rootScene.setOnMouseClicked(event -> {
            if (!settingsStage.isShowing()) {
                settingsStage.show();
            }
        });
    }

    private void wirePrimaryStage(Stage primaryStage, ConfigFilePersistence configFilePersistence, TarsosAudioEngine tarsosAudioEngine, LedDataSenderService dataSenderService, SpectralAnimator animator) {
        primaryStage.setOnCloseRequest(event -> {
            CoreConfig.windowHeight = primaryStage.getHeight();
            CoreConfig.windowWidth = primaryStage.getWidth();
            configFilePersistence.persist(CoreConfig.class, "./application.properties");
            PluginSystem.getInstance().stopAllPlugins();
            tarsosAudioEngine.stop();
            dataSenderService.stop();
            animator.stop();
            Platform.exit();

            // force exit in 5 seconds if application does not finish
            Thread exit = new Thread(() -> {
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    // we expect that the sleep will be interrupted
                    // and the application should finish before the sleep is over
                }

                LOGGER.error("Forced exit");
                System.exit(1);
            });

            exit.setDaemon(true);
            exit.start();
        });
    }

    private Scene createScene(SpectralView spectralView) {
        //create
        Scene scene = new Scene(spectralView);

        WindowProperty.widthProperty = scene.widthProperty();
        WindowProperty.heightProperty = scene.heightProperty();

        return scene;
    }

    private void configureStage(Stage stage, Scene scene) {
        // setup
        stage.setTitle("Visualiser");

        stage.setWidth(CoreConfig.windowWidth);
        stage.setHeight(CoreConfig.windowHeight);

        stage.setScene(scene);
    }

    private Stage createSettingsStage() throws IOException {
        // create
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/fxml/spectrum/spectrum.fxml"));
        Parent root = fxmlLoader.load();

        root.getStylesheets().add(getClass().getResource("/ui/fxml/spectrum/style.css").toExternalForm());
        Scene settingsScene = new Scene(root);
        Stage settingsStage = new Stage();

        // setup
        settingsStage.setTitle("Settings");
        settingsStage.setScene(settingsScene);
        settingsStage.setHeight(560);
        settingsStage.setWidth(840);

        // preload the settings page
        settingsStage.show();
        settingsStage.hide();

        return settingsStage;
    }
}
