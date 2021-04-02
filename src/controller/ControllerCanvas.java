package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import model.*;

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

    /**
     * Initialise la classe en lui passant le modèle
     * @param model
     */
    public void init(ProjectModel model){
        this.model = model;
    }

    @FXML
    public void setOnMousePressed(MouseEvent mouseEvent) {
        switch (this.model.getShapeToDraw()) {
            case Line -> lineFirstPoint(mouseEvent);
            case Square -> squareFirstPoint(mouseEvent);
            case Rectangle -> rectangleFirstPoint(mouseEvent);
            case Circle -> circleFirstPoint(mouseEvent);
            case Triangle -> triangleFirstPoint(mouseEvent);
        }
        clear();
        this.model.paintLayers();
    }



    @FXML
    public void setOnMouseDragged(MouseEvent mouseEvent) {
        switch (this.model.getShapeToDraw()) {
            case Line -> lineSetNewPoint(mouseEvent);
            case Square -> squareSetNewPoint(mouseEvent);
            case Rectangle -> rectangleSetNewPoint(mouseEvent);
            case Circle -> circleSetNewPoint(mouseEvent);
            case Triangle -> triangleSetNewPoint(mouseEvent);
        }
        clear();
        this.model.paintLayers();
    }

    @FXML
    public void setOnMouseReleased(MouseEvent mouseEvent) {
        setOnMouseDragged(mouseEvent);
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

    public void squareFirstPoint(MouseEvent e){
        this.currentLayer = new Square(e.getX(),e.getY(),0,this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void squareSetNewPoint(MouseEvent e){
        double s = Math.abs(e.getX() - currentLayer.getX());
        ((Square) currentLayer).setSide(s);
    }

    public void rectangleFirstPoint(MouseEvent e){
        this.currentLayer = new Rectangle(e.getX(),e.getY(),0,0,this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void rectangleSetNewPoint(MouseEvent e){
        double w = Math.abs(currentLayer.getX() - e.getX());
        double h = Math.abs(currentLayer.getY() - e.getY());
        ((Rectangle) currentLayer).setWidth(w);
        ((Rectangle) currentLayer).setHeight(h);
    }

    public void circleFirstPoint(MouseEvent e){
        this.currentLayer = new Circle(e.getX(),e.getY(),0,this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void circleSetNewPoint(MouseEvent e){
        double r = Math.abs(currentLayer.getX() - e.getX())/2;
        ((Circle) currentLayer).setRadius(r);
    }

    public void triangleFirstPoint(MouseEvent e){
        //TODO
    }

    public void triangleSetNewPoint(MouseEvent e){
        //TODO
    }

    public void clear(){
        this.canvas.getGraphicsContext2D().clearRect(0,0,this.canvas.getWidth(),this.canvas.getHeight());
    }
}
