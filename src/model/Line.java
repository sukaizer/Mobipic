package model;

import javafx.scene.canvas.GraphicsContext;

public class Line extends Layer{
    double x2,y2;

    public Line(double x, double y, double x2, double y2, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.x2 = x2;
        this.y2 = y2;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getY2() {
        return y2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    @Override
    public void paint() {
        this.graphicsContext.strokeLine(this.x,this.y,this.x2,this.y2);
    }

    @Override
    public boolean isIn(double x, double y) {
        double width = Math.abs(x2 - this.x);
        double height = Math.abs(y2 - this.y);
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }
}
