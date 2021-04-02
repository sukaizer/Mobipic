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
    private Stage primaryStage;
    private FileChooser fileChooser;
    private File file;
    private ProjectModel model;

    @FXML public Pane mainPane;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.model = new ProjectModel();
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Project Files","*.mbpc"),
                new FileChooser.ExtensionFilter("Image Files","*.jpg","*.png","*.jpeg","*.bmp"));

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../ressources/fxmlFiles/canvas.fxml"));
            Parent pane = loader.load();
            ControllerCanvas controllerCanvas = loader.getController();
            controllerCanvas.init(this.model);
            mainPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(Stage primaryStage){
        this.primaryStage = primaryStage;
    }

    /**
     * Ouvre la fenetre contextuelle de choix de fichiers
     */
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
        System.out.println(this.model.getShapeToDraw().toString());
    }

    @FXML
    public void quitApp(ActionEvent actionEvent) {
        this.primaryStage.close();
        //TODO ajouter etes vous sur de quitter etc
    }

    /**
     * Ouvre la fenetre contextuelle d'ajout de formes
     */
    @FXML
    public void addShapeMenu(ActionEvent actionEvent) {
        String path = "../ressources/fxmlFiles/shapes.fxml";
        String name = "Ajouter une Forme";
        FXMLLoader loader = setNewStage(path,name);
        assert loader != null;
        ControllerShapesMenu controllerShapesMenu = loader.getController();
        controllerShapesMenu.init(this.model);
    }

    /**
     * Crée une nouvelle fenetre
     * @param path chemin vers le fichier FXML
     * @param name nom de la fenetre
     * @return FXMLLoader
     */
    private FXMLLoader setNewStage(String path, String name) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.setRoot(root);
            stage.setTitle(name);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
            return loader;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
