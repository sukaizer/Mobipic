package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public final class RectangleLayer extends ShapeLayer {

    private double width;
    private double height;

    public RectangleLayer(double x, double y, double width, double height, ShapeStyle style) {
        super(x, y, style);
        this.width = width;
        this.height = height;
    }

    private RectangleLayer(RectangleLayer other) {
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

    private Box box() {
        return new Box(x, y, width, height);
    }

    private void setBox(Box box) {
        x = box.x();
        y = box.y();
        width = box.w();
        height = box.h();
    }

    @Override
    public void setFromDrag(double ax, double ay, double bx, double by) {
        setBox(Box.fromCorners(ax, ay, bx, by, false));
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        gc.fillRect(x, y, width, height);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
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
        setBox(after);
        return Box.draggedCorner(before, index, after);
    }

    @Override
    public RectangleLayer copy() {
        return new RectangleLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.rectangle";
    }
}
