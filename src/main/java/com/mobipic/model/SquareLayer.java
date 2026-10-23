package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public final class SquareLayer extends ShapeLayer {

    private double side;

    public SquareLayer(double x, double y, double side, ShapeStyle style) {
        super(x, y, style);
        this.side = side;
    }

    private SquareLayer(SquareLayer other) {
        super(other);
        this.side = other.side;
    }

    public double getSide() {
        return side;
    }

    private Box box() {
        return new Box(x, y, side, side);
    }

    private void setBox(Box box) {
        x = box.x();
        y = box.y();
        side = box.w();
    }

    @Override
    public void setFromDrag(double ax, double ay, double bx, double by) {
        setBox(Box.fromCorners(ax, ay, bx, by, true));
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        gc.fillRect(x, y, side, side);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokeRect(x, y, side, side);
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
        Box after = before.withCornerMoved(index, px, py, true);
        setBox(after);
        return Box.draggedCorner(before, index, after);
    }

    @Override
    public SquareLayer copy() {
        return new SquareLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.square";
    }
}
