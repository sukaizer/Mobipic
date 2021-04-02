package model;

import javafx.scene.canvas.GraphicsContext;

public abstract class Layer {

    protected boolean isFocused;
    protected double x;
    protected double y;
    protected boolean isFilled;
    protected GraphicsContext graphicsContext;

    public Layer(double x, double y, GraphicsContext graphicsContext) {
        this.isFilled = false;
        this.isFocused = false;
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
}
