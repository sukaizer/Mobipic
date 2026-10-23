package com.mobipic.io.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * JSON representation of a layer in {@code project.json}. Colours are {@code #rrggbbaa} strings.
 * <p>
 * Format 1 (Mobipic 2.0) fields are still read: shapes had a single {@code color} and a {@code filled}
 * flag, squares and circles had their own types, ellipses used radii and texts were placed by their baseline.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LayerDto.Image.class, name = "image"),
        @JsonSubTypes.Type(value = LayerDto.Text.class, name = "text"),
        @JsonSubTypes.Type(value = LayerDto.Rectangle.class, name = "rectangle"),
        @JsonSubTypes.Type(value = LayerDto.Ellipse.class, name = "ellipse"),
        @JsonSubTypes.Type(value = LayerDto.Line.class, name = "line"),
        @JsonSubTypes.Type(value = LayerDto.Triangle.class, name = "triangle"),
        @JsonSubTypes.Type(value = LayerDto.Path.class, name = "path"),
        @JsonSubTypes.Type(value = LayerDto.Square.class, name = "square"),
        @JsonSubTypes.Type(value = LayerDto.Circle.class, name = "circle")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface LayerDto {

    /** Properties shared by every layer; {@code null} in format 1 files. */
    Props props();

    record Props(String name, Boolean visible, Double opacity, Double rotation) {
    }

    /** Outline and fill of a shape (format 2). */
    record Stroke(String color, String fill, double lineWidth, String fillMode) {
    }

    /** @param image path of the PNG entry inside the project archive */
    record Image(Props props, double x, double y, double width, double height, String image, String filter)
            implements LayerDto {
    }

    record Text(Props props, double x, double y, String text, String family, double size, boolean bold,
                boolean italic, String alignment, String color, String outlineColor, Double outlineWidth,
                Boolean shadow) implements LayerDto {
    }

    record Rectangle(Props props, double x, double y, double width, double height, Double cornerRadius,
                     Stroke style, String color, Double lineWidth, Boolean filled) implements LayerDto {
    }

    /** Format 2: {@code width}/{@code height}; format 1: {@code radiusX}/{@code radiusY}. */
    record Ellipse(Props props, double x, double y, Double width, Double height, Double radiusX, Double radiusY,
                   Stroke style, String color, Double lineWidth, Boolean filled) implements LayerDto {
    }

    record Line(Props props, double x1, double y1, double x2, double y2, Stroke style, String color,
                Double lineWidth) implements LayerDto {
    }

    record Triangle(Props props, double x1, double y1, double x2, double y2, double x3, double y3, Stroke style,
                    String color, Double lineWidth, Boolean filled) implements LayerDto {
    }

    record Path(Props props, double[] xs, double[] ys, Stroke style) implements LayerDto {
    }

    /** Format 1 only, read as a rectangle. */
    record Square(Props props, double x, double y, double side, String color, double lineWidth, boolean filled)
            implements LayerDto {
    }

    /** Format 1 only, read as an ellipse. */
    record Circle(Props props, double x, double y, double radius, String color, double lineWidth, boolean filled)
            implements LayerDto {
    }
}
