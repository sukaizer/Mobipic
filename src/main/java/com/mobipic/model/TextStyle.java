package com.mobipic.model;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

/**
 * Font and colours of a text layer.
 *
 * @param outlineWidth width of the outline drawn around the letters, {@code 0} for none
 * @param shadow       whether a soft drop shadow is drawn under the text
 */
public record TextStyle(String family, double size, boolean bold, boolean italic, TextAlignment alignment,
                        Color color, Color outlineColor, double outlineWidth, boolean shadow) {

    public static final TextStyle DEFAULT = new TextStyle("Verdana", 36, false, false, TextAlignment.LEFT,
            Color.BLACK, Color.WHITE, 0, false);

    public TextStyle {
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(alignment, "alignment");
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(outlineColor, "outlineColor");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive: " + size);
        }
        outlineWidth = Math.max(0, outlineWidth);
    }

    public Font font() {
        return Font.font(family, bold ? FontWeight.BOLD : FontWeight.NORMAL,
                italic ? FontPosture.ITALIC : FontPosture.REGULAR, size);
    }

    public TextStyle withSize(double newSize) {
        return new TextStyle(family, newSize, bold, italic, alignment, color, outlineColor, outlineWidth, shadow);
    }

    public TextStyle withOutlineColor(Color newColor) {
        return new TextStyle(family, size, bold, italic, alignment, color, newColor, outlineWidth, shadow);
    }

    public TextStyle withColor(Color newColor) {
        return new TextStyle(family, size, bold, italic, alignment, newColor, outlineColor, outlineWidth, shadow);
    }
}
