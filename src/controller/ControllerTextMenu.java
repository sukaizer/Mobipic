package controller;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import model.Layer;
import model.ProjectModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerTextMenu implements Initializable {
    private ProjectModel model;
    private Canvas canvas;
    private ControllerCanvas controllerCanvas;
    private Layer currentLayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void init(ProjectModel model, ControllerCanvas controllerCanvas, Canvas canvas){
        this.model = model;
        this.controllerCanvas = controllerCanvas;
        this.canvas = canvas;
    }
}
