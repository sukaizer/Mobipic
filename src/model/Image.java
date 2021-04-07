package model;

import javafx.scene.canvas.GraphicsContext;

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
}
