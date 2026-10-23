package com.mobipic.model;

import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * How a shape is painted: outline colour and width, fill colour, and which of both are drawn.
 */
public record ShapeStyle(Color stroke, Color fill, double lineWidth, FillMode fillMode) {

    public static final ShapeStyle DEFAULT = new ShapeStyle(Color.BLACK, Color.WHITE, 3, FillMode.OUTLINE);

    public ShapeStyle {
        Objects.requireNonNull(stroke, "stroke");
        Objects.requireNonNull(fill, "fill");
        Objects.requireNonNull(fillMode, "fillMode");
        if (lineWidth <= 0) {
            throw new IllegalArgumentException("lineWidth must be positive: " + lineWidth);
        }
    }

    public ShapeStyle withStroke(Color color) {
        return new ShapeStyle(color, fill, lineWidth, fillMode);
    }

    public ShapeStyle withFill(Color color) {
        return new ShapeStyle(stroke, color, lineWidth, fillMode);
    }

    public ShapeStyle withLineWidth(double width) {
        return new ShapeStyle(stroke, fill, width, fillMode);
    }

    public ShapeStyle withFillMode(FillMode mode) {
        return new ShapeStyle(stroke, fill, lineWidth, mode);
    }
}
