package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ControllerCanvas implements Initializable {
    private ProjectModel model;
    private Layer currentLayer;
    private int triangleFirst;
    @FXML private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.triangleFirst = 0;
    }

    /**
     * Initialise la classe en lui passant le modèle
     *
     * @param model
     */
    public void init(ProjectModel model) {
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
            case Triangle -> {
            }
        }
        clear();
        this.model.paintLayers();
    }

    @FXML
    public void setOnMouseReleased(MouseEvent mouseEvent) {
        setOnMouseDragged(mouseEvent);
        if (this.model.getShapeToDraw().equals(ShapeToDraw.Triangle) && this.triangleFirst == 3){
            this.model.setShapeToDraw(ShapeToDraw.nothing);
        } else if (!this.model.getShapeToDraw().equals(ShapeToDraw.Triangle)){
            this.model.setShapeToDraw(ShapeToDraw.nothing);
        }
        if (this.triangleFirst == 3){
            this.triangleFirst = 0;
        }
    }

    public void lineFirstPoint(MouseEvent e) {
        resetTriangle();
        this.currentLayer = new Line(e.getX(), e.getY(), e.getX(), e.getY(), this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void lineSetNewPoint(MouseEvent e) {
        resetTriangle();
        ((Line) currentLayer).setX2(e.getX());
        ((Line) currentLayer).setY2(e.getY());
    }

    public void squareFirstPoint(MouseEvent e) {
        resetTriangle();
        this.currentLayer = new Square(e.getX(), e.getY(), 0, this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void squareSetNewPoint(MouseEvent e) {
        resetTriangle();
        double s = Math.abs(e.getX() - currentLayer.getX());
        ((Square) currentLayer).setSide(s);
    }

    public void rectangleFirstPoint(MouseEvent e) {
        resetTriangle();
        this.currentLayer = new Rectangle(e.getX(), e.getY(), 0, 0, this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void rectangleSetNewPoint(MouseEvent e) {
        resetTriangle();
        double w = Math.abs(currentLayer.getX() - e.getX());
        double h = Math.abs(currentLayer.getY() - e.getY());
        ((Rectangle) currentLayer).setWidth(w);
        ((Rectangle) currentLayer).setHeight(h);
    }


    public void circleFirstPoint(MouseEvent e) {
        resetTriangle();
        this.currentLayer = new Circle(e.getX(), e.getY(), 0, this.canvas.getGraphicsContext2D());
        this.model.addLayer(currentLayer);
    }

    public void circleSetNewPoint(MouseEvent e) {
        resetTriangle();
        double r = Math.abs(currentLayer.getX() - e.getX()) / 2;
        ((Circle) currentLayer).setRadius(r);
    }

    public void triangleFirstPoint(MouseEvent e) {
        if (this.triangleFirst == 0) {
            this.currentLayer = new Triangle(e.getX(), e.getY(), e.getX(), e.getY(), e.getX(), e.getY(), this.canvas.getGraphicsContext2D());
            this.model.addLayer(currentLayer);
        } else if (this.triangleFirst == 1){
            ((Triangle)this.currentLayer).setX2(e.getX());
            ((Triangle)this.currentLayer).setY2(e.getY());
        } else {
            ((Triangle)this.currentLayer).setX3(e.getX());
            ((Triangle)this.currentLayer).setY3(e.getY());
        }
        this.triangleFirst++;
    }

    public void clear() {
        this.canvas.getGraphicsContext2D().clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private void resetTriangle() {
        if (this.triangleFirst != 0) {
            this.triangleFirst = 0;
            this.model.getLayerArrayList().remove(this.model.getLayerArrayList().size() - 1);
            this.model.paintLayers();
        }
    }
}
