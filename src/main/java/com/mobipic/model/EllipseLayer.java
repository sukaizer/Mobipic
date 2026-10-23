package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * An ellipse defined by the top-left corner of its bounding box and its two radii.
 */
public final class EllipseLayer extends ShapeLayer {

    private double radiusX;
    private double radiusY;

    public EllipseLayer(double x, double y, double radiusX, double radiusY, ShapeStyle style) {
        super(x, y, style);
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    private EllipseLayer(EllipseLayer other) {
        super(other);
        this.radiusX = other.radiusX;
        this.radiusY = other.radiusY;
    }

    public double getRadiusX() {
        return radiusX;
    }

    public double getRadiusY() {
        return radiusY;
    }

    private Box box() {
        return new Box(x, y, 2 * radiusX, 2 * radiusY);
    }

    private void setBox(Box box) {
        x = box.x();
        y = box.y();
        radiusX = box.w() / 2;
        radiusY = box.h() / 2;
    }

    @Override
    public void setFromDrag(double ax, double ay, double bx, double by) {
        setBox(Box.fromCorners(ax, ay, bx, by, false));
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        gc.fillOval(x, y, 2 * radiusX, 2 * radiusY);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokeOval(x, y, 2 * radiusX, 2 * radiusY);
    }

    @Override
    public boolean contains(double px, double py) {
        if (radiusX <= 0 || radiusY <= 0) {
            return false;
        }
        double dx = (px - (x + radiusX)) / radiusX;
        double dy = (py - (y + radiusY)) / radiusY;
        return dx * dx + dy * dy <= 1;
    }

    @Override
    public List<Point2D> handles() {
        return box().corners();
    }

    @Override
    public int moveHandle(int index, double px, double py) {
        Box before = box();
        Box after = before.withCornerMoved(index, px, py, false);
        setBox(after);
        return Box.draggedCorner(before, index, after);
    }

    @Override
    public EllipseLayer copy() {
        return new EllipseLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.ellipse";
    }
}
