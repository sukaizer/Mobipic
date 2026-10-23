package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Objects;

/**
 * A bitmap image drawn in a box, with an optional colour filter on top.
 * The underlying {@link Image} is immutable and shared between copies.
 */
public final class ImageLayer extends Layer {

    private final Image image;
    private double width;
    private double height;
    private ColorFilter filter = ColorFilter.NONE;

    public ImageLayer(Image image, double x, double y) {
        this(image, x, y, image.getWidth(), image.getHeight());
    }

    public ImageLayer(Image image, double x, double y, double width, double height) {
        super(x, y);
        this.image = Objects.requireNonNull(image, "image");
        this.width = width;
        this.height = height;
    }

    private ImageLayer(ImageLayer other) {
        this(other.image, other.x, other.y, other.width, other.height);
        this.filter = other.filter;
    }

    public Image getImage() {
        return image;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public ColorFilter getFilter() {
        return filter;
    }

    public void setFilter(ColorFilter filter) {
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    private Box box() {
        return new Box(x, y, width, height);
    }

    @Override
    public void paint(GraphicsContext gc) {
        gc.drawImage(image, x, y, width, height);
        if (filter != ColorFilter.NONE) {
            gc.setFill(filter.overlay());
            gc.fillRect(x, y, width, height);
        }
    }

    @Override
    public void strokeOutline(GraphicsContext gc) {
        gc.strokeRect(x, y, width, height);
    }

    @Override
    public boolean contains(double px, double py) {
        return box().contains(px, py);
    }

    @Override
    public List<Point2D> handles() {
        return box().corners();
    }

    @Override
    public int moveHandle(int index, double px, double py) {
        Box before = box();
        Box after = before.withCornerMoved(index, px, py, false);
        x = after.x();
        y = after.y();
        width = after.w();
        height = after.h();
        return Box.draggedCorner(before, index, after);
    }

    @Override
    public ImageLayer copy() {
        return new ImageLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.image";
    }
}
