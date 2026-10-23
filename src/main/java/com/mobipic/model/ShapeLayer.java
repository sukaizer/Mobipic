package com.mobipic.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A geometric shape drawn with a colour, a stroke width and optionally filled.
 */
public abstract sealed class ShapeLayer extends Layer
        permits SquareLayer, RectangleLayer, CircleLayer, EllipseLayer, LineLayer, TriangleLayer {

    protected Color color;
    protected double lineWidth;
    protected boolean filled;

    protected ShapeLayer(double x, double y, ShapeStyle style) {
        super(x, y);
        setStyle(style);
    }

    protected ShapeLayer(ShapeLayer other) {
        super(other.x, other.y);
        this.color = other.color;
        this.lineWidth = other.lineWidth;
        this.filled = other.filled;
    }

    public ShapeStyle getStyle() {
        return new ShapeStyle(color, lineWidth, filled);
    }

    public void setStyle(ShapeStyle style) {
        this.color = style.color();
        this.lineWidth = style.lineWidth();
        this.filled = style.filled() && canFill();
    }

    public Color getColor() {
        return color;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public boolean isFilled() {
        return filled;
    }

    @Override
    public final void paint(GraphicsContext gc) {
        gc.setLineWidth(lineWidth);
        if (filled) {
            gc.setFill(color);
            fillGeometry(gc);
        } else {
            gc.setStroke(color);
            strokeGeometry(gc);
        }
    }

    @Override
    public void strokeOutline(GraphicsContext gc) {
        strokeGeometry(gc);
    }

    /**
     * Sets the geometry from a mouse drag going from the anchor {@code (ax, ay)} to {@code (bx, by)}.
     */
    public abstract void setFromDrag(double ax, double ay, double bx, double by);

    protected abstract void fillGeometry(GraphicsContext gc);

    protected abstract void strokeGeometry(GraphicsContext gc);

    protected boolean canFill() {
        return true;
    }

    /** @return the tolerance around thin geometry when hit testing */
    protected double hitTolerance() {
        return Math.max(lineWidth / 2, 4);
    }
}
