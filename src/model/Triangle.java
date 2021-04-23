package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Triangle extends Layer {

    private double x2, y2, x3, y3;
    private int point;

    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3, GraphicsContext graphicsContext) {
        super(x1,y1,graphicsContext);
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    public Triangle(Triangle triangle){
        super(triangle.x,triangle.y,triangle.graphicsContext);
        this.x2 = triangle.x2;
        this.y2 = triangle.y2;
        this.x3 = triangle.x3;
        this.y3 = triangle.y3;
        this.isFilled = triangle.isFilled;
        this.lineWidth = triangle.lineWidth;
        this.color = triangle.color;
        this.point = triangle.point;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
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
        x[0] = this.x;
        x[1] = this.x2;
        x[2] = this.x3;
        double[] y = new double[3];
        y[0] = this.y;
        y[1] = this.y2;
        y[2] = this.y3;
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
    public boolean isIn(double xS, double yS) {
        double xA = this.x;
        double yA = this.y;
        double xB = this.x2 - this.x;
        double yB = this.y2 - this.y;
        double xC = this.x3 - this.x;
        double yC = this.y3 - this.y;
        double x = xS - this.x;
        double y = yS - this.y;

        double d = xB*yC - xC*yB;

        double wA = (x*(yB-yC) + y*(xC-xB) + xB*yC - xC*yB)/d;
        double wB = (x*yC - y*xC)/d;
        double wC = (y*xB - yB*x)/d;

        return(wA > 0 && wA < 1 && wB > 0 && wB < 1 && wC > 0 && wC < 1);
    }

    @Override
    public String toString() {
        return "Triangle";
    }

    @Override
    public Triangle setSamePositions() {
        Triangle layer1 = new Triangle(this.getX(), this.getY(), this.getX2(), this.getY2(), this.getX3(), this.getY3(), graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(3);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }

    @Override
    public String save() {
        return "Triangle " + this.x + " " + this.y + " " + this.x2 + " " + this.y2 + " " + this.x3 + " " + this.y3 + " " + this.color + " " + this.lineWidth + " " + this.isFilled;
    }
}
