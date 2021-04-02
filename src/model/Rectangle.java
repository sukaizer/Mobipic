package model;

import javafx.scene.canvas.GraphicsContext;

public class Rectangle extends Layer{

    private double height, width;

    Rectangle(double x, double y, double h, double w, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.height = h;
        this.width = w;
    }

    double getHeight(){
        return this.height;
    }

    double getWidth(){
        return this.width;
    }

    void setHeight(double h){
        this.height = h;
    }

    void setWidth(double w) {
        this.width = w;
    }

    @Override
    public void paint() {
        if (this.isFilled){
            this.graphicsContext.fillRect(this.x, this.y, this.width, this.height);
        } else {
            this.graphicsContext.strokeRect(this.x, this.y, this.width, this.height);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }
}
