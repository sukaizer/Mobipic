package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * A triangle defined by its three vertices {@code (x, y)}, {@code (x2, y2)} and {@code (x3, y3)}.
 */
public final class TriangleLayer extends ShapeLayer {

    private double x2;
    private double y2;
    private double x3;
    private double y3;

    public TriangleLayer(double x, double y, double x2, double y2, double x3, double y3, ShapeStyle style) {
        super(x, y, style);
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    private TriangleLayer(TriangleLayer other) {
        super(other);
        this.x2 = other.x2;
        this.y2 = other.y2;
        this.x3 = other.x3;
        this.y3 = other.y3;
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public double getX3() {
        return x3;
    }

    public double getY3() {
        return y3;
    }

    @Override
    protected boolean usesRotation() {
        return false;
    }

    /** Sets an isosceles triangle inscribed in the box, pointing up. */
    public void setBox(Box box) {
        x = box.x() + box.w() / 2;
        y = box.y();
        x2 = box.maxX();
        y2 = box.maxY();
        x3 = box.x();
        y3 = box.maxY();
    }

    /** Moves the vertex {@code index} (0, 1 or 2). */
    public void setVertex(int index, double vx, double vy) {
        switch (index) {
            case 0 -> {
                x = vx;
                y = vy;
            }
            case 1 -> {
                x2 = vx;
                y2 = vy;
            }
            case 2 -> {
                x3 = vx;
                y3 = vy;
            }
            default -> throw new IndexOutOfBoundsException(index);
        }
    }

    @Override
    public Box bounds() {
        return Box.around(xs(), ys());
    }

    @Override
    public void translate(double dx, double dy) {
        super.translate(dx, dy);
        x2 += dx;
        y2 += dy;
        x3 += dx;
        y3 += dy;
    }

    @Override
    public void rotateBy(double degrees, double px, double py) {
        Rotate r = new Rotate(degrees, px, py);
        List<Point2D> vertices = handles();
        for (int i = 0; i < 3; i++) {
            Point2D p = r.transform(vertices.get(i));
            setVertex(i, p.getX(), p.getY());
        }
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        gc.fillPolygon(xs(), ys(), 3);
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        gc.strokePolygon(xs(), ys(), 3);
    }

    @Override
    public void strokeOutline(GraphicsContext gc) {
        strokeGeometry(gc);
    }

    private double[] xs() {
        return new double[]{x, x2, x3};
    }

    private double[] ys() {
        return new double[]{y, y2, y3};
    }

    @Override
    protected boolean contains(double px, double py) {
        double tolerance = hitTolerance();
        if (Geometry.distanceToSegment(px, py, x, y, x2, y2) <= tolerance
                || Geometry.distanceToSegment(px, py, x2, y2, x3, y3) <= tolerance
                || Geometry.distanceToSegment(px, py, x3, y3, x, y) <= tolerance) {
            return true;
        }
        if (Geometry.cross(x, y, x2, y2, x3, y3) == 0) {
            return false; // degenerate triangle: only its edges can be hit
        }
        double d1 = Geometry.cross(px, py, x, y, x2, y2);
        double d2 = Geometry.cross(px, py, x2, y2, x3, y3);
        double d3 = Geometry.cross(px, py, x3, y3, x, y);
        boolean hasNegative = d1 < 0 || d2 < 0 || d3 < 0;
        boolean hasPositive = d1 > 0 || d2 > 0 || d3 > 0;
        return !(hasNegative && hasPositive);
    }

    @Override
    public List<Point2D> handles() {
        return List.of(new Point2D(x, y), new Point2D(x2, y2), new Point2D(x3, y3));
    }

    @Override
    protected int moveHandle(int index, double lx, double ly, boolean keepRatio) {
        setVertex(index, lx, ly);
        return index;
    }

    @Override
    public TriangleLayer copy() {
        return new TriangleLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.triangle";
    }
}
