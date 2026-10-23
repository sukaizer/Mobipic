package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * A straight line from {@code (x, y)} to {@code (x2, y2)}. Lines are never filled.
 */
public final class LineLayer extends ShapeLayer {

    private double x2;
    private double y2;

    public LineLayer(double x, double y, double x2, double y2, ShapeStyle style) {
        super(x, y, style);
        this.x2 = x2;
        this.y2 = y2;
    }

    private LineLayer(LineLayer other) {
        super(other);
        this.x2 = other.x2;
        this.y2 = other.y2;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    @Override
    public void translate(double dx, double dy) {
        super.translate(dx, dy);
        x2 += dx;
        y2 += dy;
    }

    @Override
    public void setFromDrag(double ax, double ay, double bx, double by) {
        x = ax;
        y = ay;
        x2 = bx;
        y2 = by;
    }

    @Override
    protected boolean canFill() {
        return false;
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        strokeGeometry(gc);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokeLine(x, y, x2, y2);
    }

    @Override
    public boolean contains(double px, double py) {
        return Geometry.distanceToSegment(px, py, x, y, x2, y2) <= hitTolerance();
    }

    @Override
    public List<Point2D> handles() {
        return List.of(new Point2D(x, y), new Point2D(x2, y2));
    }

    @Override
    public int moveHandle(int index, double px, double py) {
        switch (index) {
            case 0 -> {
                x = px;
                y = py;
            }
            case 1 -> {
                x2 = px;
                y2 = py;
            }
            default -> throw new IndexOutOfBoundsException(index);
        }
        return index;
    }

    @Override
    public LineLayer copy() {
        return new LineLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.line";
    }
}
