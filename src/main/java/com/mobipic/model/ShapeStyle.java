package com.mobipic.model;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Colour, stroke width and fill option of a shape.
 */
public record ShapeStyle(Color color, double lineWidth, boolean filled) {

    public static final ShapeStyle DEFAULT = new ShapeStyle(Color.BLACK, 1.0, false);

    public ShapeStyle {
        Objects.requireNonNull(color, "color");
        if (lineWidth <= 0) {
            throw new IllegalArgumentException("lineWidth must be positive: " + lineWidth);
        }
    }
}
