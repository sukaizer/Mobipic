package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * A rectangle (or square), optionally with rounded corners.
 */
public final class RectangleLayer extends ShapeLayer {

    private double width;
    private double height;
    private double cornerRadius;

    public RectangleLayer(double x, double y, double width, double height, ShapeStyle style) {
        this(x, y, width, height, 0, style);
    }

    public RectangleLayer(double x, double y, double width, double height, double cornerRadius, ShapeStyle style) {
        super(x, y, style);
        this.width = width;
        this.height = height;
        setCornerRadius(cornerRadius);
    }

    private RectangleLayer(RectangleLayer other) {
        super(other);
        this.width = other.width;
        this.height = other.height;
        this.cornerRadius = other.cornerRadius;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(double cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
    }

    /** Sets the geometry from a drawn or resized box. */
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
        double arc = effectiveArc();
        if (arc > 0) {
            gc.fillRoundRect(x, y, width, height, arc, arc);
        } else {
            gc.fillRect(x, y, width, height);
        }
    }

    @Override
    protected void strokeGeometry(GraphicsContext gc) {
        double arc = effectiveArc();
        if (arc > 0) {
            gc.strokeRoundRect(x, y, width, height, arc, arc);
        } else {
            gc.strokeRect(x, y, width, height);
        }
    }

    /** @return the corner arc diameter, limited so that corners never overlap */
    private double effectiveArc() {
        return Math.min(2 * cornerRadius, Math.min(width, height));
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
        setBox(after);
        return Box.draggedHandle(before, index, after);
    }

    @Override
    public RectangleLayer copy() {
        return new RectangleLayer(this);
    }

    @Override
    public String typeKey() {
        if (cornerRadius > 0) {
            return "layer.roundedRectangle";
        }
        return width == height ? "layer.square" : "layer.rectangle";
    }
}
