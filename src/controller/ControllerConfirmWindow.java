package controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerConfirmWindow implements Initializable {

    @FXML
    public Label label;
    ControllerMain controllerMain;
    int n;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(ControllerMain controllerMain, int n){
        this.controllerMain = controllerMain;
        this.n = n;
        if (this.n == 0) {
            this.label.setText("Êtes-vous sûr de vouloir quitter l'application ?");
        } else if (this.n == 1) {
            this.label.setText("Projet déjà ouvert, voulez-vous en ouvrir un autre ?");
        } else if (this.n == 2) {
            this.label.setText("Projet déjà ouvert, voulez-vous en commencer un autre ?");
        }
    }

    @FXML
    public void yes(Event actionEvent) throws FileNotFoundException {
        if (this.n == 0) {
            this.controllerMain.primaryStage.close();
            Node source = (Node) actionEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        } else if (this.n == 1){
            Node source = (Node) actionEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            this.controllerMain.getControllerCanvas().clear();
            stage.close();
            this.controllerMain.openProject(actionEvent);
        } else if (this.n == 2){
            Node source = (Node) actionEvent.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            this.controllerMain.getControllerCanvas().clear();
            stage.close();
            this.controllerMain.newProject(actionEvent);
        }
    }

    @FXML
    public void no(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
