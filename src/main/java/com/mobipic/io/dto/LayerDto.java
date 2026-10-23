package com.mobipic.io.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * JSON representation of a layer in {@code project.json}. Colours are {@code #rrggbbaa} strings.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LayerDto.Image.class, name = "image"),
        @JsonSubTypes.Type(value = LayerDto.Text.class, name = "text"),
        @JsonSubTypes.Type(value = LayerDto.Square.class, name = "square"),
        @JsonSubTypes.Type(value = LayerDto.Rectangle.class, name = "rectangle"),
        @JsonSubTypes.Type(value = LayerDto.Circle.class, name = "circle"),
        @JsonSubTypes.Type(value = LayerDto.Ellipse.class, name = "ellipse"),
        @JsonSubTypes.Type(value = LayerDto.Line.class, name = "line"),
        @JsonSubTypes.Type(value = LayerDto.Triangle.class, name = "triangle")
})
public sealed interface LayerDto {

    /** @param image path of the PNG entry inside the project archive */
    record Image(double x, double y, double width, double height, String image, String filter)
            implements LayerDto {
    }

    record Text(double x, double y, String text, String family, boolean bold, boolean italic,
                double size, String color) implements LayerDto {
    }

    record Square(double x, double y, double side, String color, double lineWidth, boolean filled)
            implements LayerDto {
    }

    record Rectangle(double x, double y, double width, double height, String color, double lineWidth,
                     boolean filled) implements LayerDto {
    }

    record Circle(double x, double y, double radius, String color, double lineWidth, boolean filled)
            implements LayerDto {
    }

    record Ellipse(double x, double y, double radiusX, double radiusY, String color, double lineWidth,
                   boolean filled) implements LayerDto {
    }

    record Line(double x1, double y1, double x2, double y2, String color, double lineWidth)
            implements LayerDto {
    }

    record Triangle(double x1, double y1, double x2, double y2, double x3, double y3, String color,
                    double lineWidth, boolean filled) implements LayerDto {
    }
}
