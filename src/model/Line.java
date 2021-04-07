package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line extends Layer{
    double x2,y2;

    public Line(double x, double y, double x2, double y2, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(Line line){
        super(line.x, line.y, line.graphicsContext);
        this.x2 = line.x2;
        this.y2 = line.y2;
        this.isFilled = line.isFilled;
        this.lineWidth = line.lineWidth;
        this.color = line.color;
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
        this.graphicsContext.setStroke(this.color);
        this.graphicsContext.setLineWidth(this.lineWidth);
        this.graphicsContext.strokeLine(this.x,this.y,this.x2,this.y2);
    }

    @Override
    public boolean isIn(double x, double y) {
        double width = Math.abs(this.x2 - this.x);
        double height = Math.abs(this.y2 - this.y);

        return x >= Math.min(this.x,this.x2) && x <= Math.min(this.x,this.x2) + width && y >= Math.min(this.y,this.y2) && y <= Math.min(this.y,this.y2) + height;
    }

    @Override
    public String toString() {
        return "Ligne";
    }

    @Override
    public Line setSamePositions() {
        Line layer1 = new Line(this.getX(),this.getY(), this.getX2(), this.getY2(), graphicsContext);
        layer1.setLineWidth(3);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }
}
