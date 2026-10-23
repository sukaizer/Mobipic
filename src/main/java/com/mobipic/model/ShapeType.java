package com.mobipic.model;

import java.util.Locale;

/**
 * The kinds of shapes the user can draw.
 */
public enum ShapeType {
    LINE, SQUARE, RECTANGLE, CIRCLE, ELLIPSE, TRIANGLE;

    /** @return a new, empty shape anchored at the given point */
    public ShapeLayer create(double x, double y, ShapeStyle style) {
        return switch (this) {
            case LINE -> new LineLayer(x, y, x, y, style);
            case SQUARE -> new SquareLayer(x, y, 0, style);
            case RECTANGLE -> new RectangleLayer(x, y, 0, 0, style);
            case CIRCLE -> new CircleLayer(x, y, 0, style);
            case ELLIPSE -> new EllipseLayer(x, y, 0, 0, style);
            case TRIANGLE -> new TriangleLayer(x, y, x, y, x, y, style);
        };
    }

    /** @return the i18n key of the drawing instructions for this shape */
    public String hintKey() {
        return "shape.hint." + name().toLowerCase(Locale.ROOT);
    }
}
