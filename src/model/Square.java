package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Square extends Layer{

    private double side;

    public Square(double x, double y, double s, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.side = s;
    }

    public double getSide(){
        return this.side;
    }

    public void setSide(double s) {
        this.side = s;
    }

    @Override
    public void paint() {
        if(this.isFilled){
            this.graphicsContext.setFill(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.fillRect(this.x, this.y, this.side, this.side);
        } else{
            this.graphicsContext.setStroke(this.color);
            this.graphicsContext.setLineWidth(this.lineWidth);
            this.graphicsContext.strokeRect(this.x, this.y, this.side, this.side);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        return x >= this.x && x <= this.x + this.side && y >= this.y && y <= this.y + this.side;
    }

    @Override
    public String toString() {
        return "Carré";
    }

    @Override
    public Square setSamePositions() {
        Square layer1 = new Square(this.getX(), this.getY(), this.getSide(), graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(3);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }
}
