package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import model.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerCanvas implements Initializable {
    private ProjectModel model;
    private Layer currentLayer;
    private int triangleFirst;
    @FXML
    private Canvas canvas;

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

    public void init(Layer currentLayer){
        this.currentLayer = currentLayer;
    }
    @FXML
    public void setOnMousePressed(MouseEvent mouseEvent) {
        if (this.model.isNotEditing()) return;
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
        if (this.model.isNotEditing()) return;
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
        if (this.model.isNotEditing()) return;
        setOnMouseDragged(mouseEvent);
        if (this.model.getShapeToDraw().equals(ShapeToDraw.Triangle) && this.triangleFirst == 3) {
            this.model.setShapeToDraw(ShapeToDraw.nothing);
            this.model.setEditing(false);
        } else if (!this.model.getShapeToDraw().equals(ShapeToDraw.Triangle)) {
            this.model.setShapeToDraw(ShapeToDraw.nothing);
            this.model.setEditing(false);
        }
        if (this.triangleFirst == 3) {
            this.triangleFirst = 0;
        }
        this.model.layersToString();
    }

    public void lineFirstPoint(MouseEvent e) {
        resetTriangle();
        currentLayer.setX(e.getX());
        currentLayer.setY(e.getY());
        ((Line) currentLayer).setX2(e.getX());
        ((Line) currentLayer).setY2(e.getY());
        this.model.addLayer(currentLayer);
    }

    public void lineSetNewPoint(MouseEvent e) {
        resetTriangle();
        ((Line) currentLayer).setX2(e.getX());
        ((Line) currentLayer).setY2(e.getY());
    }

    public void squareFirstPoint(MouseEvent e) {
        resetTriangle();
        currentLayer.setX(e.getX());
        currentLayer.setY(e.getY());
        ((Square) currentLayer).setSide(0);
        this.model.addLayer(currentLayer);
    }

    public void squareSetNewPoint(MouseEvent e) {
        resetTriangle();
        double s = Math.abs(e.getX() - currentLayer.getX());
        ((Square) currentLayer).setSide(s);
    }

    public void rectangleFirstPoint(MouseEvent e) {
        resetTriangle();
        currentLayer.setX(e.getX());
        currentLayer.setY(e.getY());
        ((Rectangle) currentLayer).setWidth(0);
        ((Rectangle) currentLayer).setHeight(0);
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
        currentLayer.setX(e.getX());
        currentLayer.setY(e.getY());
        ((Circle) currentLayer).setRadius(0);
        this.model.addLayer(currentLayer);
    }

    public void circleSetNewPoint(MouseEvent e) {
        resetTriangle();
        double r = Math.abs(currentLayer.getX() - e.getX()) / 2;
        ((Circle) currentLayer).setRadius(r);
    }

    public void triangleFirstPoint(MouseEvent e) {
        if (this.triangleFirst == 0) {
            ((Triangle) currentLayer).setX1(e.getX());
            ((Triangle) currentLayer).setY1(e.getY());
            ((Triangle) currentLayer).setX2(e.getX());
            ((Triangle) currentLayer).setY2(e.getY());
            ((Triangle) currentLayer).setX3(e.getX());
            ((Triangle) currentLayer).setY3(e.getY());
            this.model.addLayer(currentLayer);
        } else if (this.triangleFirst == 1) {
            ((Triangle) this.currentLayer).setX2(e.getX());
            ((Triangle) this.currentLayer).setY2(e.getY());
        } else {
            ((Triangle) this.currentLayer).setX3(e.getX());
            ((Triangle) this.currentLayer).setY3(e.getY());
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
            this.model.getLayers().remove(this.model.getLayers().size() - 1);
            this.model.paintLayers();
        }
    }
}
