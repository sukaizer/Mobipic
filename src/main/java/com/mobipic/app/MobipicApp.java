package com.mobipic.app;

import com.mobipic.controller.MainController;
import com.mobipic.util.Fxml;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Application entry point: shows the splash screen, then the main window.
 * A project or image path given on the command line is opened at startup.
 */
public class MobipicApp extends Application {

    private static final Duration SPLASH_DURATION = Duration.seconds(2);

    @Override
    public void start(Stage splashStage) {
        Image icon = Fxml.image("iconeMobi.png");

        Parent splash = Fxml.<Object>load("start.fxml").root();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setTitle("Mobipic");
        splashStage.getIcons().add(icon);
        splashStage.setScene(new Scene(splash));
        splashStage.centerOnScreen();
        splashStage.show();

        Fxml.View<MainController> main = Fxml.load("main.fxml");
        Stage stage = new Stage();
        stage.setTitle("Mobipic");
        stage.getIcons().add(icon);
        stage.setScene(new Scene(main.root()));
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        main.controller().init(stage, getHostServices());

        Runnable showMain = () -> {
            if (stage.isShowing()) {
                return;
            }
            splashStage.close();
            stage.show();
            stage.centerOnScreen();
            startupFile().ifPresent(main.controller()::openFile);
        };
        splash.setOnMouseClicked(e -> showMain.run()); // click to skip
        PauseTransition pause = new PauseTransition(SPLASH_DURATION);
        pause.setOnFinished(e -> showMain.run());
        pause.play();
    }

    private java.util.Optional<Path> startupFile() {
        List<String> args = getParameters().getUnnamed();
        return args.stream().map(Path::of).filter(Files::isRegularFile).findFirst();
    }

    public static void main(String[] args) {
        launch(MobipicApp.class, args);
    }
}
