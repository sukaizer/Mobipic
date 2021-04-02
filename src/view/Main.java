package view;

import controller.ControllerMain;
import controller.ControllerShapesMenu;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("../ressources/fxmlFiles/main.fxml"));
        Parent root = loader.load();

        ControllerMain controllerMain = loader.getController();
        controllerMain.init(primaryStage);
        Scene scene = new Scene(root);
        scene.setRoot(root);

        primaryStage.setTitle("Mobipic");
        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
