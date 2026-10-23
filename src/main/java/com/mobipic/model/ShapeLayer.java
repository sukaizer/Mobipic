package com.mobipic.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.Objects;

/**
 * A geometric shape painted with an outline, a fill, or both.
 */
public abstract sealed class ShapeLayer extends Layer
        permits RectangleLayer, EllipseLayer, LineLayer, TriangleLayer, PathLayer {

    protected ShapeStyle style;

    protected ShapeLayer(double x, double y, ShapeStyle style) {
        super(x, y);
        setStyle(style);
    }

    protected ShapeLayer(ShapeLayer other) {
        super(other);
        this.style = other.style;
    }

    public ShapeStyle getStyle() {
        return style;
    }

    public void setStyle(ShapeStyle style) {
        Objects.requireNonNull(style, "style");
        // Open shapes (lines, brush strokes) are always drawn as an outline
        this.style = canFill() ? style : style.withFillMode(FillMode.OUTLINE);
    }

    /** @return whether the shape is closed and can be filled */
    public boolean canFill() {
        return true;
    }

    @Override
    public final void paint(GraphicsContext gc) {
        if (style.fillMode().filled()) {
            gc.setFill(style.fill());
            fillGeometry(gc);
        }
        if (style.fillMode().stroked()) {
            gc.setStroke(style.stroke());
            gc.setLineWidth(style.lineWidth());
            if (!canFill()) { // open strokes look better with round ends
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.setLineJoin(StrokeLineJoin.ROUND);
            }
            strokeGeometry(gc);
        }
    }

    protected abstract void fillGeometry(GraphicsContext gc);

    protected abstract void strokeGeometry(GraphicsContext gc);

    /** @return the tolerance around thin geometry when hit testing */
    protected double hitTolerance() {
        return Math.max(style.lineWidth() / 2, 4);
    }
}
