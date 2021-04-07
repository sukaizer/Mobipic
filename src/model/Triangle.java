package model;

import javafx.scene.canvas.GraphicsContext;

public class Triangle extends Layer {

    private double x1, y1, x2, y2, x3, y3;

    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3, GraphicsContext graphicsContext) {
        super(graphicsContext);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    public double getX1() {
        return this.x1;
    }

    public double getY1() {
        return this.y1;
    }

    public double getX2() {
        return this.x2;
    }

    public double getY2() {
        return this.y2;
    }

    public double getX3() {
        return this.x3;
    }

    public double getY3() {
        return this.y3;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public void setY1(double y1) {
        this.y1 = y1;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public void setX3(double x3) {
        this.x3 = x3;
    }

    public void setY3(double y3) {
        this.y3 = y3;
    }


    @Override
    public void paint() {
        double[] x = new double[3];
        x[0] = x1;
        x[1] = x2;
        x[2] = x3;
        double[] y = new double[3];
        y[0] = y1;
        y[1] = y2;
        y[2] = y3;
        if (this.isFilled){
            this.graphicsContext.setFill(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.fillPolygon(x, y, 3);
        } else {
            this.graphicsContext.setStroke(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.strokePolygon(x, y, 3);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        double xa = Math.min(this.x1, Math.min(this.x2, this.x3));
        double xb = Math.max(this.x1, Math.min(this.x2, this.x3));
        double ya = Math.min(this.y1, Math.min(this.y2, this.y3));
        double yb = Math.min(this.y1, Math.min(this.y2, this.y3));
        return x >= xa && x <= xb && y >= ya && y <= yb;
    }

    @Override
    public String toString() {
        return "Triangle";
    }
}
