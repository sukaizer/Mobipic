package com.mobipic.app;

import com.mobipic.controller.MainController;
import com.mobipic.util.Fxml;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
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
    private static final double MIN_WIDTH = 640;
    private static final double MIN_HEIGHT = 420;

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
        fitToScreen(stage);
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

    /** Uses the preferred size of the window when the screen allows it, or most of the screen otherwise. */
    private static void fitToScreen(Stage stage) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Parent root = stage.getScene().getRoot();
        stage.setWidth(Math.min(root.prefWidth(-1), screen.getWidth() * 0.92));
        stage.setHeight(Math.min(root.prefHeight(-1), screen.getHeight() * 0.92));
        stage.setMinWidth(Math.min(MIN_WIDTH, screen.getWidth()));
        stage.setMinHeight(Math.min(MIN_HEIGHT, screen.getHeight()));
    }

    private java.util.Optional<Path> startupFile() {
        List<String> args = getParameters().getUnnamed();
        return args.stream().map(Path::of).filter(Files::isRegularFile).findFirst();
    }

    public static void main(String[] args) {
        launch(MobipicApp.class, args);
    }
}
