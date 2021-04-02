package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import model.Layer;
import model.Line;
import model.ProjectModel;
import model.ShapeToDraw;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerCanvas implements Initializable {
    private ProjectModel model;
    private Layer currentLayer;
    @FXML private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void init(ProjectModel model){
        this.model = model;
    }

    @FXML
    public void setOnMousePressed(MouseEvent mouseEvent) {
        if(this.model.getShapeToDraw().equals(ShapeToDraw.Line)){
            lineFirstPoint(mouseEvent);
        }
        clear();
        this.model.paintLayers();
    }

    @FXML
    public void setOnMouseDragged(MouseEvent mouseEvent) {
        if(this.model.getShapeToDraw().equals(ShapeToDraw.Line)){
            lineSetNewPoint(mouseEvent);
        }
        clear();
        this.model.paintLayers();
    }



    @FXML
    public void setOnMouseReleased(MouseEvent mouseEvent) {
        if(this.model.getShapeToDraw().equals(ShapeToDraw.Line)){
            lineSetNewPoint(mouseEvent);
        }
        clear();
        this.model.paintLayers();
        this.model.setShapeToDraw(ShapeToDraw.nothing);
    }


    public void lineFirstPoint(MouseEvent e){
        this.currentLayer = new Line(e.getX(),e.getY(),e.getX(),e.getY(),this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void lineSetNewPoint(MouseEvent e){
        ((Line) currentLayer).setX2(e.getX());
        ((Line) currentLayer).setY2(e.getY());
    }

    public void clear(){
        this.canvas.getGraphicsContext2D().clearRect(0,0,this.canvas.getWidth(),this.canvas.getHeight());
    }
}
