package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.ProjectModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControllerMain implements Initializable {
    FileChooser fileChooser;
    File file;
    ProjectModel model;

    @FXML public Pane mainPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Project Files","*.mbpc"),
                new FileChooser.ExtensionFilter("Image Files","*.jpg","*.png","*.jpeg","*.bmp"));
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("../ressources/fxmlFiles/canvas.fxml"));
            mainPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void setOpenMenuAction(ActionEvent e) {
        file = fileChooser.showOpenDialog(new Stage());
        try {
            String filename = file.getName();
            String extension = Optional.of(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1)).get();
            if(extension.equals("jpg") || extension.equals("png") || extension.equals("jpeg") || extension.equals("bmp")){
                System.out.println("hey");
            }
        }catch(NullPointerException ignored){}
    }

    @FXML
    public void addShapeMenu(ActionEvent actionEvent) {
        String path = "../ressources/fxmlFiles/shapes.fxml";
        String name = "Ajouter une Forme";
        setNewStage(path,name);
    }

    private void setNewStage(String path, String name) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.setRoot(root);
            stage.setTitle(name);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
