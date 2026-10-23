package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * A circle defined by the top-left corner of its bounding box and its radius.
 */
public final class CircleLayer extends ShapeLayer {

    private double radius;

    public CircleLayer(double x, double y, double radius, ShapeStyle style) {
        super(x, y, style);
        this.radius = radius;
    }

    private CircleLayer(CircleLayer other) {
        super(other);
        this.radius = other.radius;
    }

    public double getRadius() {
        return radius;
    }

    private Box box() {
        return new Box(x, y, 2 * radius, 2 * radius);
    }

    private void setBox(Box box) {
        x = box.x();
        y = box.y();
        radius = box.w() / 2;
    }

    @Override
    public void setFromDrag(double ax, double ay, double bx, double by) {
        setBox(Box.fromCorners(ax, ay, bx, by, true));
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        gc.fillOval(x, y, 2 * radius, 2 * radius);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokeOval(x, y, 2 * radius, 2 * radius);
    }

    @Override
    public boolean contains(double px, double py) {
        double dx = px - (x + radius);
        double dy = py - (y + radius);
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public List<Point2D> handles() {
        return box().corners();
    }

    @Override
    public int moveHandle(int index, double px, double py) {
        Box before = box();
        Box after = before.withCornerMoved(index, px, py, true);
        setBox(after);
        return Box.draggedCorner(before, index, after);
    }

    @Override
    public CircleLayer copy() {
        return new CircleLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.circle";
    }
}
