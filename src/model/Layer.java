package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public abstract class Layer {

    protected boolean isFocused;
    protected double x;
    protected double y;
    protected boolean isFilled;
    protected double lineWidth;
    protected Paint color;
    protected GraphicsContext graphicsContext;
    protected boolean isMoving;
    protected boolean isResizing;

    public Layer(double x, double y, GraphicsContext graphicsContext) {
        this.isFilled = false;
        this.isFocused = false;
        this.isMoving = false;
        this.isResizing = false;
        this.lineWidth = 1.0;
        this.color = Color.BLACK;
        this.x = x;
        this.y = y;
        this.graphicsContext = graphicsContext;
    }

    public Layer(GraphicsContext graphicsContext) {
        this.isFilled = false;
        this.isFocused = false;
        this.graphicsContext = graphicsContext;
    }

    /**
     * Dessine la forme
     */
    public abstract void paint();

    /**
     * renvoie l'objet GraphicsContext relié
     * @return GraphicsContext
     */
    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }

    /**
     * détermine si l'objet est cliqué ou pas
     * @param focus
     */
    public void setFocused(boolean focus){
        this.isFocused = focus;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public abstract boolean isIn(double x, double y);

    public abstract String toString();

    public void setFilled(boolean filled) {
        isFilled = filled;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public void setColor(Paint color) {
        this.color = color;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public boolean isResizing() {
        return isResizing;
    }

    public void setResizing(boolean resizing) {
        isResizing = resizing;
    }

    public void resetModifiers(){
        this.isResizing = false;
        this.isMoving = false;
    }

    public abstract Layer setSamePositions();
}
