package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Image extends Layer {
    private javafx.scene.image.Image image;
    private double w;
    private double h;

    public Image(javafx.scene.image.Image image, double x, double y, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.image = image;
        this.w = image.getWidth();
        this.h = image.getHeight();
    }

    public double getW() {
        return w;
    }

    public double getH() {
        return h;
    }

    @Override
    public void paint() {
        this.graphicsContext.drawImage(this.image, this.x, this.y);
    }

    @Override
    public boolean isIn(double x, double y) {
        return x >= this.x && x <= this.x + this.image.getWidth() && y >= this.y && y <= this.y + this.image.getHeight();
    }

    @Override
    public String toString() {
        return "Image";
    }

    @Override
    public Rectangle setSamePositions() {
        Rectangle layer1 = new Rectangle(this.getX(), this.getY(), this.image.getHeight(), this.image.getWidth(), graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(3);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }
}
