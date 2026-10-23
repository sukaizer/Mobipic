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
        super(other);
        this.image = other.image;
        this.width = other.width;
        this.height = other.height;
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

    @Override
    public Box bounds() {
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
    protected boolean contains(double lx, double ly) {
        return bounds().contains(lx, ly);
    }

    @Override
    public List<Point2D> handles() {
        return bounds().handles();
    }

    @Override
    protected int oppositeHandle(int index) {
        return Box.oppositeHandle(index);
    }

    @Override
    protected int moveHandle(int index, double lx, double ly, boolean keepRatio) {
        Box before = bounds();
        Box after = before.withHandleMoved(index, lx, ly, keepRatio);
        x = after.x();
        y = after.y();
        width = after.w();
        height = after.h();
        return Box.draggedHandle(before, index, after);
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
