package view;

import controller.ControllerMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.FileInputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader1 = new FXMLLoader();
        loader1.setLocation(getClass().getResource("../ressources/fxmlFiles/start.fxml"));
        Parent root1 = loader1.load();
        Scene scene1 = new Scene(root1);
        scene1.setRoot(root1);
        primaryStage.setTitle("Mobipic");
        Image icon = new Image(new FileInputStream("src/ressources/images/iconeMobi.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene1);
        primaryStage.show();


        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../ressources/fxmlFiles/main.fxml"));
        Parent root = loader.load();
        ControllerMain controllerMain = loader.getController();
        Stage stage = new Stage();
        controllerMain.init(stage);
        Scene scene = new Scene(root);
        scene.setRoot(root);
        stage.setTitle("Mobipic");
        stage.centerOnScreen();
        stage.getIcons().add(icon);
        stage.setScene(scene);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    primaryStage.close();
                    stage.show();
                });
            } catch (Exception ignored){}
        }).start();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
