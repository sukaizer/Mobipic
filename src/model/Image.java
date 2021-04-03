package model;

import javafx.scene.canvas.GraphicsContext;

public class Image extends Layer {
    private javafx.scene.image.Image image;
    private double w;
    private double h;

    public Image(javafx.scene.image.Image image, double x, double y, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.image = image;
    }

    @Override
    public void paint() {
        this.graphicsContext.drawImage(this.image, this.x, this.y);
    }

    @Override
    public boolean isIn(double x, double y) {
        return false;
    }
}
