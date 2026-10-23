package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;

import java.util.Arrays;
import java.util.List;

/**
 * A freehand brush stroke: a smoothed polyline. It can be moved, resized (scaled) and rotated like any shape.
 */
public final class PathLayer extends ShapeLayer {

    private double[] xs;
    private double[] ys;
    private int size;

    public PathLayer(double x, double y, ShapeStyle style) {
        super(x, y, style);
        xs = new double[16];
        ys = new double[16];
        xs[0] = x;
        ys[0] = y;
        size = 1;
    }

    /** Restores a stroke from its points (at least one). */
    public PathLayer(double[] xs, double[] ys, ShapeStyle style) {
        super(xs[0], ys[0], style);
        if (xs.length != ys.length || xs.length == 0) {
            throw new IllegalArgumentException("A path needs at least one point, with as many x as y");
        }
        this.xs = xs.clone();
        this.ys = ys.clone();
        this.size = xs.length;
    }

    private PathLayer(PathLayer other) {
        super(other);
        this.xs = Arrays.copyOf(other.xs, other.size);
        this.ys = Arrays.copyOf(other.ys, other.size);
        this.size = other.size;
    }

    public void addPoint(double px, double py) {
        if (size == xs.length) {
            xs = Arrays.copyOf(xs, size * 2);
            ys = Arrays.copyOf(ys, size * 2);
        }
        xs[size] = px;
        ys[size] = py;
        size++;
    }

    public int pointCount() {
        return size;
    }

    public double[] getXs() {
        return Arrays.copyOf(xs, size);
    }

    public double[] getYs() {
        return Arrays.copyOf(ys, size);
    }

    /** @return the last point, used to skip points too close to it */
    public Point2D lastPoint() {
        return new Point2D(xs[size - 1], ys[size - 1]);
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
        return Box.around(getXs(), getYs());
    }

    @Override
    public void translate(double dx, double dy) {
        for (int i = 0; i < size; i++) {
            xs[i] += dx;
            ys[i] += dy;
        }
        syncOrigin();
    }

    @Override
    public void rotateBy(double degrees, double px, double py) {
        Rotate r = new Rotate(degrees, px, py);
        for (int i = 0; i < size; i++) {
            Point2D p = r.transform(xs[i], ys[i]);
            xs[i] = p.getX();
            ys[i] = p.getY();
        }
        syncOrigin();
    }

    private void syncOrigin() {
        x = xs[0];
        y = ys[0];
    }

    @Override
    protected void fillGeometry(GraphicsContext gc) {
        // never filled
    }

    /** Draws a smooth curve through the midpoints of consecutive points. */
    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        if (size == 1) {
            double r = style.lineWidth() / 2;
            gc.setFill(style.stroke());
            gc.fillOval(xs[0] - r, ys[0] - r, 2 * r, 2 * r);
            return;
        }
        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < size - 1; i++) {
            gc.quadraticCurveTo(xs[i], ys[i], (xs[i] + xs[i + 1]) / 2, (ys[i] + ys[i + 1]) / 2);
        }
        gc.lineTo(xs[size - 1], ys[size - 1]);
        gc.stroke();
    }

    @Override
    protected boolean contains(double lx, double ly) {
        double tolerance = hitTolerance();
        if (size == 1) {
            return Math.hypot(lx - xs[0], ly - ys[0]) <= tolerance;
        }
        for (int i = 1; i < size; i++) {
            if (Geometry.distanceToSegment(lx, ly, xs[i - 1], ys[i - 1], xs[i], ys[i]) <= tolerance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Point2D> handles() {
        return bounds().handles();
    }

    @Override
    protected int oppositeHandle(int index) {
        return Box.oppositeHandle(index);
    }

    /** Resizing scales every point from the old bounds to the new ones. */
    @Override
    protected int moveHandle(int index, double lx, double ly, boolean keepRatio) {
        Box before = bounds();
        Box after = before.withHandleMoved(index, lx, ly, keepRatio);
        // Keep the drawing mirrored when the box is flipped, like other shapes
        boolean flipX = (index == 0 || index == 6 || index == 7) ? lx > before.maxX()
                : (index == 2 || index == 3 || index == 4) && lx < before.x();
        boolean flipY = (index == 0 || index == 1 || index == 2) ? ly > before.maxY()
                : (index == 4 || index == 5 || index == 6) && ly < before.y();
        for (int i = 0; i < size; i++) {
            xs[i] = scale(xs[i], before.x(), before.w(), after.x(), after.w(), flipX);
            ys[i] = scale(ys[i], before.y(), before.h(), after.y(), after.h(), flipY);
        }
        syncOrigin();
        return Box.draggedHandle(before, index, after);
    }

    private static double scale(double v, double from, double fromSize, double to, double toSize, boolean flip) {
        if (fromSize == 0) {
            return to + toSize / 2;
        }
        double t = (v - from) / fromSize;
        return to + (flip ? 1 - t : t) * toSize;
    }

    @Override
    public PathLayer copy() {
        return new PathLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.path";
    }
}
