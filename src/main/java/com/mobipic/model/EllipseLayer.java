package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * An ellipse (or circle) inscribed in its bounding box.
 */
public final class EllipseLayer extends ShapeLayer {

    private double width;
    private double height;

    public EllipseLayer(double x, double y, double width, double height, ShapeStyle style) {
        super(x, y, style);
        this.width = width;
        this.height = height;
    }

    private EllipseLayer(EllipseLayer other) {
        super(other);
        this.width = other.width;
        this.height = other.height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setBox(Box box) {
        x = box.x();
        y = box.y();
        width = box.w();
        height = box.h();
    }

    @Override
    public Box bounds() {
        return new Box(x, y, width, height);
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        gc.fillOval(x, y, width, height);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokeOval(x, y, width, height);
    }

    @Override
    protected boolean contains(double lx, double ly) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        double dx = (lx - (x + width / 2)) / (width / 2);
        double dy = (ly - (y + height / 2)) / (height / 2);
        return dx * dx + dy * dy <= 1;
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
        setBox(after);
        return Box.draggedHandle(before, index, after);
    }

    @Override
    public EllipseLayer copy() {
        return new EllipseLayer(this);
    }

    @Override
    public String typeKey() {
        return width == height ? "layer.circle" : "layer.ellipse";
    }
}
