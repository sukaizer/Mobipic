package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.Stage;
import model.ProjectModel;
import model.ShapeToDraw;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerShapesMenu implements Initializable {
    ProjectModel model;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void init(ProjectModel model){
        this.model = model;
    }

    @FXML
    public void drawLine(ActionEvent actionEvent) {
        this.model.setShapeToDraw(ShapeToDraw.Line);
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
