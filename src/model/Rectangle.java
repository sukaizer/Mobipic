package model;

import javafx.scene.canvas.GraphicsContext;

public class Rectangle extends Layer{

    private double height, width;

    public Rectangle(double x, double y, double h, double w, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.height = h;
        this.width = w;
    }

    public double getHeight(){
        return this.height;
    }

    public double getWidth(){
        return this.width;
    }

    public void setHeight(double h){
        this.height = h;
    }

    public void setWidth(double w) {
        this.width = w;
    }

    @Override
    public void paint() {
        if (this.isFilled){
            this.graphicsContext.setFill(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.fillRect(this.x, this.y, this.width, this.height);
        } else {
            this.graphicsContext.setStroke(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.strokeRect(this.x, this.y, this.width, this.height);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    @Override
    public String toString() {
        return "Rectangle";
    }
}
