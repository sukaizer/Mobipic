package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ellipse extends Layer{
    private double r1;
    private double r2;

    public Ellipse(double x, double y, double r1, double r2,GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.r1 = r1;
        this.r2 = r2;
    }

    public Ellipse(Ellipse ellipse){
        super(ellipse.x, ellipse.y, ellipse.graphicsContext);
        this.r1 = ellipse.r1;
        this.r2 = ellipse.r2;
        this.isFilled = ellipse.isFilled;
        this.lineWidth = ellipse.lineWidth;
        this.color = ellipse.color;
    }

    public double getR1() {
        return r1;
    }

    public double getR2() {
        return r2;
    }

    public void setR1(double r1) {
        this.r1 = r1;
    }

    public void setR2(double r2) {
        this.r2 = r2;
    }

    @Override
    public void paint() {
        if(this.isFilled){
            this.graphicsContext.setFill(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.fillOval(this.x, this.y, 2*this.r1, 2*this.r2);
        } else{
            this.graphicsContext.setStroke(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.strokeOval(this.x, this.y, 2*this.r1, 2*this.r2);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        double xc = this.x + this.r1;
        double yc = this.y + this.r2;
        return ((x-xc)*(x-xc))/(r1*r1)+((y-yc)*(y-yc))/(r2*r2) <= 1;
    }

    @Override
    public String toString() {
        return "Ellipse";
    }

    @Override
    public Ellipse setSamePositions() {
        Ellipse layer1 = new Ellipse(this.getX(), this.getY(), this.getR1(),this.r2,graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(3);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }

    @Override
    public Layer copy() {
        Ellipse layer1 = new Ellipse(this.getX(), this.getY(), this.getR1(),this.getR2() ,graphicsContext);
        layer1.setFilled(this.isFilled);
        layer1.setLineWidth(this.lineWidth);
        layer1.setColor(this.color);
        return layer1;
    }

    @Override
    public String save() {
        return "Ellipse" + "\n" +this.x + "\n" + this.y + "\n" + this.r1 + "\n" + this.r2 + "\n" + this.color + "\n" + this.lineWidth + "\n" + this.isFilled;
    }
}
