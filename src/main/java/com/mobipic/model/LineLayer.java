package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * A straight line from {@code (x, y)} to {@code (x2, y2)}. Lines are always drawn as an outline.
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

    public void setPoints(double x, double y, double x2, double y2) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public boolean canFill() {
        return false;
    }

    @Override
    protected boolean usesRotation() {
        return false;
    }

    @Override
    public Box bounds() {
        return Box.around(new double[]{x, x2}, new double[]{y, y2});
    }

    @Override
    public void translate(double dx, double dy) {
        super.translate(dx, dy);
        x2 += dx;
        y2 += dy;
    }

    @Override
    public void rotateBy(double degrees, double px, double py) {
        Rotate r = new Rotate(degrees, px, py);
        Point2D a = r.transform(x, y);
        Point2D b = r.transform(x2, y2);
        setPoints(a.getX(), a.getY(), b.getX(), b.getY());
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        // never filled
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokeLine(x, y, x2, y2);
    }

    @Override
    public void strokeOutline(GraphicsContext gc) {
        strokeGeometry(gc);
    }

    @Override
    protected boolean contains(double lx, double ly) {
        return Geometry.distanceToSegment(lx, ly, x, y, x2, y2) <= hitTolerance();
    }

    @Override
    public List<Point2D> handles() {
        return List.of(new Point2D(x, y), new Point2D(x2, y2));
    }

    @Override
    protected int moveHandle(int index, double lx, double ly, boolean keepRatio) {
        switch (index) {
            case 0 -> {
                Point2D p = keepRatio ? Geometry.snapAngle(x2, y2, lx, ly) : new Point2D(lx, ly);
                x = p.getX();
                y = p.getY();
            }
            case 1 -> {
                Point2D p = keepRatio ? Geometry.snapAngle(x, y, lx, ly) : new Point2D(lx, ly);
                x2 = p.getX();
                y2 = p.getY();
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
