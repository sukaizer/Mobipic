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
        this.color = Color.color(0,0,0,0);
    }

    public Image(Image image){
        super(image.x, image.y, image.graphicsContext);
        this.image = image.image;
        this.w = image.image.getWidth();
        this.h = image.image.getHeight();
        this.isFilled = image.isFilled;
        this.lineWidth = image.lineWidth;
        this.color = image.color;
    }

    public double getW() {
        return w;
    }

    public double getH() {
        return h;
    }

    public void setW(double w) {
        this.w = w;
    }

    public void setH(double h) {
        this.h = h;
    }

    @Override
    public void paint() {
        this.graphicsContext.drawImage(this.image, this.x, this.y, this.w, this.h);
        this.graphicsContext.setFill(this.color);
        this.graphicsContext.fillRect(this.x,this.y,this.w,this.h);
    }

    @Override
    public boolean isIn(double x, double y) {
        return x >= this.x && x <= this.x + this.getW() && y >= this.y && y <= this.y + this.getH();
    }

    @Override
    public String toString() {
        if (this.isBaseLayer()){
            return "Base Image";
        }
        return "Image";
    }

    @Override
    public Rectangle setSamePositions() {
        Rectangle layer1 = new Rectangle(this.getX(), this.getY(), this.h, this.w, graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(6);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }

    @Override
    public String save() {
        return "Image " + this.x + " " + this.y + " " + this.w + " " + this.h + " " + this.color + " " + this.lineWidth;
    }

    public javafx.scene.image.Image getImage() {
        return image;
    }
}
