package model;

import javafx.scene.canvas.GraphicsContext;

public class Circle extends Layer{
    private double r;

    public Circle(double x, double y, double r, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.r = r;
    }

    public double getRadius() {
        return r;
    }

    public void setRadius(double r) {
        this.r = r;
    }

    @Override
    public void paint() {
        if(this.isFilled){
            this.graphicsContext.setFill(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.fillOval(this.x, this.y, 2*this.r, 2*this.r);
        } else{
            this.graphicsContext.setStroke(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.strokeOval(this.x, this.y, 2*this.r, 2*this.r);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        double xc = this.x + this.r;
        double yc = this.y + this.r;
        return (x-xc)*(x-xc)+(y-yc)*(y-yc) <= this.r*this.r;
    }
}
