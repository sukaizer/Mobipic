package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerCanvas implements Initializable {
    private ProjectModel model;
    private Layer currentLayer;
    private int triangleFirst;
    private double lastX;
    private double lastY;
    private double side;
    private double width;
    private double height;
    private Stage primaryStage;
    private boolean resizing;

    @FXML
    private Canvas canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.triangleFirst = 0;
        this.lastX = 0;
        this.lastY = 0;
    }

    /**
     * Initialise la classe en lui passant le modèle
     *
     * @param model
     */
    public void init(ProjectModel model, Stage primaryStage) {
        this.model = model;
        this.primaryStage = primaryStage;
        this.resizing = false;
    }

    public void init(Layer currentLayer){
        this.currentLayer = currentLayer;
    }


    @FXML
    public void mouseEntered(MouseEvent e) {
        if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isMoving() && this.model.getEditingLayer().isIn(e.getX(),e.getY())) {
            this.primaryStage.getScene().setCursor(Cursor.OPEN_HAND);
        }
        if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isMoving() && !this.model.getEditingLayer().isIn(e.getX(),e.getY())) {
            this.primaryStage.getScene().setCursor(Cursor.DEFAULT);
        }
        if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isResizing() && isTouchingEdge(e,this.model.getEditingLayer())) {
            this.primaryStage.getScene().setCursor(Cursor.CROSSHAIR);
        }
        if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isResizing() && !isTouchingEdge(e,this.model.getEditingLayer())) {
            this.primaryStage.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    @FXML
    public void setOnMousePressed(MouseEvent mouseEvent) {
        if (this.model.getLayers().size() == 0) {
            this.model.resetHelpLayer();
        }
        if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isMoving() && this.model.getEditingLayer().isIn(mouseEvent.getX(),mouseEvent.getY())) {
            this.moveShapeFirst(mouseEvent);
            this.model.setHelpLayer(this.model.getEditingLayer().setSamePositions());
            clear();
            this.model.paintLayers();
            this.model.getHelpLayer().paint();
        } else if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isResizing() && isTouchingEdge(mouseEvent,this.model.getEditingLayer())) {
            this.resizeShapeFirst(mouseEvent);
            this.model.setHelpLayer(this.model.getEditingLayer().setSamePositions());
            this.resizing = true;
            clear();
            this.model.paintLayers();
            this.model.getHelpLayer().paint();
            paintResizeSquare();
        } else if (this.model.isNotEditing()) {}
        else {
            switch (this.model.getShapeToDraw()) {
                case Line -> lineFirstPoint(mouseEvent);
                case Square -> squareFirstPoint(mouseEvent);
                case Rectangle -> rectangleFirstPoint(mouseEvent);
                case Circle -> circleFirstPoint(mouseEvent);
                case Triangle -> triangleFirstPoint(mouseEvent);
                case Ellipse -> ellipseFirstPoint(mouseEvent);
            }
            clear();
            this.model.paintLayers();
        }
    }


    @FXML
    public void setOnMouseDragged(MouseEvent mouseEvent) {
        if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isMoving() && this.model.getEditingLayer().isIn(mouseEvent.getX(),mouseEvent.getY())) {
            this.moveShapeSecond(mouseEvent);
            this.model.setHelpLayer(this.model.getEditingLayer().setSamePositions());
            clear();
            this.model.paintLayers();
            this.model.getHelpLayer().paint();
        } else if (this.model.getEditingLayer() != null && this.model.getEditingLayer().isResizing() && this.resizing) {
            this.resizeShapeSecond(mouseEvent);
            this.model.setHelpLayer(this.model.getEditingLayer().setSamePositions());
            clear();
            this.model.paintLayers();
            this.model.getHelpLayer().paint();
            paintResizeSquare();
        } else if (this.model.isNotEditing()){}
         else {
            switch (this.model.getShapeToDraw()) {
                case Line -> lineSetNewPoint(mouseEvent);
                case Square -> squareSetNewPoint(mouseEvent);
                case Rectangle -> rectangleSetNewPoint(mouseEvent);
                case Circle -> circleSetNewPoint(mouseEvent);
                case Ellipse -> ellipseSetNewPoint(mouseEvent);
                case Triangle -> {
                }
            }
            clear();
            this.model.paintLayers();
        }
    }

    @FXML
    public void setOnMouseReleased(MouseEvent mouseEvent) {
        this.resizing = false;
        this.model.resetEditingLayer();
        this.primaryStage.getScene().setCursor(Cursor.DEFAULT);
        clear();
        this.model.paintLayers();
        try{
            this.model.getHelpLayer().paint();
        } catch (NullPointerException ignored){}
        if (this.model.isNotEditing()) {
            return;
        }
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
        //this.model.layersToString();
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

    public void ellipseFirstPoint(MouseEvent e) {
        resetTriangle();
        currentLayer.setX(e.getX());
        currentLayer.setY(e.getY());
        ((Ellipse) currentLayer).setR1(0);
        ((Ellipse) currentLayer).setR2(0);
        this.model.addLayer(currentLayer);
    }

    public void ellipseSetNewPoint(MouseEvent e) {
        resetTriangle();
        double r1 = Math.abs(currentLayer.getX() - e.getX()) / 2;
        double r2 = Math.abs(currentLayer.getY() - e.getY()) / 2;
        ((Ellipse) currentLayer).setR1(r1);
        ((Ellipse) currentLayer).setR2(r2);
    }

    public void triangleFirstPoint(MouseEvent e) {
        if (this.triangleFirst == 0) {
            ((Triangle) currentLayer).setX(e.getX());
            ((Triangle) currentLayer).setY(e.getY());
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
            this.model.deleteLastLayer();
            this.model.paintLayers();
        }
    }

    public void moveShapeFirst(MouseEvent e){
        this.lastX = e.getX();
        this.lastY = e.getY();
    }

    public void moveShapeSecond(MouseEvent e){
        if (this.model.getEditingLayer() instanceof Triangle){
            this.model.getEditingLayer().setX(this.model.getEditingLayer().getX() + (e.getX() - this.lastX));
            this.model.getEditingLayer().setY(this.model.getEditingLayer().getY() + (e.getY() - this.lastY));
            ((Triangle) this.model.getEditingLayer()).setX2(((Triangle) this.model.getEditingLayer()).getX2() + (e.getX() - this.lastX));
            ((Triangle) this.model.getEditingLayer()).setY2(((Triangle) this.model.getEditingLayer()).getY2() + (e.getY() - this.lastY));
            ((Triangle) this.model.getEditingLayer()).setX3(((Triangle) this.model.getEditingLayer()).getX3() + (e.getX() - this.lastX));
            ((Triangle) this.model.getEditingLayer()).setY3(((Triangle) this.model.getEditingLayer()).getY3() + (e.getY() - this.lastY));
        } else if (this.model.getEditingLayer() instanceof Line){
            this.model.getEditingLayer().setX(this.model.getEditingLayer().getX() + (e.getX() - this.lastX));
            this.model.getEditingLayer().setY(this.model.getEditingLayer().getY() + (e.getY() - this.lastY));
            ((Line) this.model.getEditingLayer()).setX2(((Line) this.model.getEditingLayer()).getX2() + (e.getX() - this.lastX));
            ((Line) this.model.getEditingLayer()).setY2(((Line) this.model.getEditingLayer()).getY2() + (e.getY() - this.lastY));
        } else {
            this.model.getEditingLayer().setX(this.model.getEditingLayer().getX() + (e.getX() - this.lastX));
            this.model.getEditingLayer().setY(this.model.getEditingLayer().getY() + (e.getY() - this.lastY));
        }
        this.lastX = e.getX();
        this.lastY = e.getY();
        this.model.paintLayers();
    }

    public void resizeShapeFirst(MouseEvent e){
        if (isTouchingEdge(e,this.model.getEditingLayer())) {
            if (this.model.getEditingLayer() instanceof Square) {
                this.side = ((Square) this.model.getEditingLayer()).getSide();
            } else if (this.model.getEditingLayer() instanceof Rectangle) {
                this.width = ((Rectangle) this.model.getEditingLayer()).getWidth();
                this.height = ((Rectangle) this.model.getEditingLayer()).getHeight();
            } else if (this.model.getEditingLayer() instanceof Circle) {
                this.side = ((Circle) this.model.getEditingLayer()).getRadius();
            } else if (this.model.getEditingLayer() instanceof Ellipse) {
                this.width = ((Ellipse) this.model.getEditingLayer()).getR1();
                this.height = ((Ellipse) this.model.getEditingLayer()).getR2();
            } else if (this.model.getEditingLayer() instanceof Image) {
                this.width = ((Image) this.model.getEditingLayer()).getW();
                this.height = ((Image) this.model.getEditingLayer()).getH();
            }
        }
    }

    public void resizeShapeSecond(MouseEvent e) {
        if (this.model.getEditingLayer() instanceof Square) {
            double d = Math.max(e.getX() - this.model.getEditingLayer().getX(),e.getY() - this.model.getEditingLayer().getY());
            ((Square) this.model.getEditingLayer()).setSide(Math.abs((this.side + d)));
        } else if (this.model.getEditingLayer() instanceof Rectangle) {
            double w = e.getX() - this.model.getEditingLayer().getX();
            double h = e.getY() - this.model.getEditingLayer().getY();
            ((Rectangle) this.model.getEditingLayer()).setWidth(Math.abs((this.width + w)));
            ((Rectangle) this.model.getEditingLayer()).setHeight(Math.abs((this.height + h)));
        } else if (this.model.getEditingLayer() instanceof Image) {
            double w = e.getX() - this.model.getEditingLayer().getX();
            double h = e.getY() - this.model.getEditingLayer().getY();
            ((Image) this.model.getEditingLayer()).setW(Math.abs((this.width + w)));
            ((Image) this.model.getEditingLayer()).setH(Math.abs((this.height + h)));
        } else if (this.model.getEditingLayer() instanceof Circle) {
            double d = Math.max(e.getX() - this.model.getEditingLayer().getX(),e.getY() - this.model.getEditingLayer().getY());
            ((Circle) this.model.getEditingLayer()).setRadius(Math.abs((this.side + d)));
        } else if (this.model.getEditingLayer() instanceof Ellipse){
            double r1 = e.getX() - this.model.getEditingLayer().getX();
            double r2 = e.getY() - this.model.getEditingLayer().getY();
            ((Ellipse) this.model.getEditingLayer()).setR1(Math.abs((this.width + r1)));
            ((Ellipse) this.model.getEditingLayer()).setR2(Math.abs((this.height + r2)));
        } else if (this.model.getEditingLayer() instanceof Line) {
            if (((Line) this.model.getEditingLayer()).getPoint() == 1){
                this.model.getEditingLayer().setX(e.getX());
                this.model.getEditingLayer().setY(e.getY());
            } else if (((Line) this.model.getEditingLayer()).getPoint() == 2){
                ((Line) this.model.getEditingLayer()).setX2(e.getX());
                ((Line) this.model.getEditingLayer()).setY2(e.getY());
            }
        } else if (this.model.getEditingLayer() instanceof Triangle) {
            if (((Triangle) this.model.getEditingLayer()).getPoint() == 1){
                this.model.getEditingLayer().setX(e.getX());
                this.model.getEditingLayer().setY(e.getY());
            } else if (((Triangle) this.model.getEditingLayer()).getPoint() == 2){
                ((Triangle) this.model.getEditingLayer()).setX2(e.getX());
                ((Triangle) this.model.getEditingLayer()).setY2(e.getY());
            } else if (((Triangle) this.model.getEditingLayer()).getPoint() == 3){
                ((Triangle) this.model.getEditingLayer()).setX3(e.getX());
                ((Triangle) this.model.getEditingLayer()).setY3(e.getY());
            }
        }
        clear();
        this.model.paintLayers();
    }

    public boolean isTouchingEdge(MouseEvent e, Layer layer){
        if (layer instanceof Triangle) {
            boolean b1 = ((e.getX() >= layer.getX() - 5 && e.getX() <= layer.getX() + 5) && (e.getY() >= layer.getY() - 5 && e.getY() <= layer.getY() + 5));
            boolean b2 = ((e.getX() >= ((Triangle) layer).getX2() - 5 && e.getX() <= ((Triangle) layer).getX2() + 5) && (e.getY() >= ((Triangle) layer).getY2() - 5 && e.getY() <= ((Triangle) layer).getY2() + 5));
            boolean b3 = ((e.getX() >= ((Triangle) layer).getX3() - 5 && e.getX() <= ((Triangle) layer).getX3() + 5) && (e.getY() >= ((Triangle) layer).getY3() - 5 && e.getY() <= ((Triangle) layer).getY3() + 5));
            if (b1){
                ((Triangle) layer).setPoint(1);
                return true;
            }
            if (b2){
                ((Triangle) layer).setPoint(2);
                return true;
            }
            if (b3){
                ((Triangle) layer).setPoint(3);
                return true;
            }
            return false;
        } else if (layer instanceof Line) {
            boolean b1 = ((e.getX() >= layer.getX() - 5 && e.getX() <= layer.getX() + 5) && (e.getY() >= layer.getY() - 5 && e.getY() <= layer.getY() + 5));
            boolean b2 = ((e.getX() >= ((Line) layer).getX2() - 5 & e.getX() <= ((Line) layer).getX2() + 5) && (e.getY() >= ((Line) layer).getY2() - 5 && e.getY() <= ((Line) layer).getY2() + 5));
            if (b1){
                ((Line) layer).setPoint(1);
                return true;
            }
            if (b2){
                ((Line) layer).setPoint(2);
                return true;
            }
            return false;
        }
        return ((e.getX() >= layer.getX() - 5 && e.getX() <= layer.getX() + 5) && (e.getY() >= layer.getY() - 5 && e.getY() <= layer.getY() + 5));
    }

    public void paintResizeSquare(){
        if (this.model.getEditingLayer() instanceof Triangle){
            Square l1 = new Square(this.model.getEditingLayer().getX() - 5, this.model.getEditingLayer().getY() - 5,10,this.canvas.getGraphicsContext2D());
            Square l2 = new Square(((Triangle) this.model.getEditingLayer()).getX2() - 5, ((Triangle) this.model.getEditingLayer()).getY2() - 5,10,this.canvas.getGraphicsContext2D());
            Square l3 = new Square(((Triangle) this.model.getEditingLayer()).getX3() - 5, ((Triangle) this.model.getEditingLayer()).getY3() - 5,10,this.canvas.getGraphicsContext2D());
            l1.setFilled(true);
            l2.setFilled(true);
            l3.setFilled(true);
            l1.paint();
            l2.paint();
            l3.paint();
        } else if(this.model.getEditingLayer() instanceof Line){
            Square l1 = new Square(this.model.getEditingLayer().getX() - 5, this.model.getEditingLayer().getY() - 5,10,this.canvas.getGraphicsContext2D());
            Square l2 = new Square(((Line) this.model.getEditingLayer()).getX2() - 5, ((Line) this.model.getEditingLayer()).getY2() - 5,10,this.canvas.getGraphicsContext2D());
            l1.setFilled(true);
            l2.setFilled(true);
            l1.paint();
            l2.paint();
        } else {
            Square l1 = new Square(this.model.getEditingLayer().getX() - 5, this.model.getEditingLayer().getY() - 5,10,this.canvas.getGraphicsContext2D());
            l1.setFilled(true);
            l1.paint();
        }
    }


}
