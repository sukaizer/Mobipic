package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.w3c.dom.css.Rect;

public class Rectangle extends Layer{

    private double height, width;

    public Rectangle(double x, double y, double h, double w, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.height = h;
        this.width = w;
    }

    public Rectangle(Rectangle rectangle){
        super(rectangle.x, rectangle.y, rectangle.graphicsContext);
        this.height = rectangle.height;
        this.width = rectangle.width;
        this.isFilled = rectangle.isFilled;
        this.lineWidth = rectangle.lineWidth;
        this.color = rectangle.color;
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

    @Override
    public Rectangle setSamePositions() {
        Rectangle layer1 = new Rectangle(this.getX(), this.getY(), this.getHeight(), this.getWidth(), graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(3);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }

    @Override
    public Layer copy() {
        Rectangle layer1 = new Rectangle(this.getX(), this.getY(), this.height, this.width, graphicsContext);
        layer1.setFilled(this.isFilled);
        layer1.setLineWidth(this.lineWidth);
        layer1.setColor(this.color);
        return layer1;
    }

    @Override
    public String save() {
        return "Rectangle" + "\n" + this.x + "\n" + this.y + "\n" + this.width + "\n" + this.height + "\n" + this.color + "\n" + this.lineWidth + "\n" + this.isFilled;
    }
}
