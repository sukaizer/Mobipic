package com.mobipic.model;

import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

/**
 * A single line of text whose baseline starts at {@code (x, y)}. Text layers are moved, not resized.
 */
public final class TextLayer extends Layer {

    private final String text;
    private final String family;
    private final boolean bold;
    private final boolean italic;
    private final double size;
    private final Color color;

    /** Derived from the fields above, computed lazily. */
    private Font font;
    private Bounds localBounds;

    public TextLayer(double x, double y, String text, String family, boolean bold, boolean italic,
                     double size, Color color) {
        super(x, y);
        this.text = Objects.requireNonNull(text, "text");
        this.family = Objects.requireNonNull(family, "family");
        this.bold = bold;
        this.italic = italic;
        this.size = size;
        this.color = Objects.requireNonNull(color, "color");
    }

    private TextLayer(TextLayer other) {
        this(other.x, other.y, other.text, other.family, other.bold, other.italic, other.size, other.color);
        this.font = other.font;
        this.localBounds = other.localBounds;
    }

    public String getText() {
        return text;
    }

    public String getFamily() {
        return family;
    }

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public double getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }

    public Font getFont() {
        if (font == null) {
            font = Font.font(family, bold ? FontWeight.BOLD : FontWeight.NORMAL,
                    italic ? FontPosture.ITALIC : FontPosture.REGULAR, size);
        }
        return font;
    }

    /** @return the bounds of the text relative to its baseline origin */
    private Bounds localBounds() {
        if (localBounds == null) {
            Text node = new Text(text);
            node.setFont(getFont());
            node.setTextOrigin(VPos.BASELINE);
            localBounds = node.getLayoutBounds();
        }
        return localBounds;
    }

    private Box box() {
        Bounds b = localBounds();
        return new Box(x + b.getMinX(), y + b.getMinY(), b.getWidth(), b.getHeight());
    }

    @Override
    public void paint(GraphicsContext gc) {
        gc.setFont(getFont());
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
        gc.setFill(color);
        gc.fillText(text, x, y);
    }

    @Override
    public void strokeOutline(GraphicsContext gc) {
        Box box = box();
        gc.strokeRect(box.x(), box.y(), box.w(), box.h());
    }

    @Override
    public boolean contains(double px, double py) {
        return box().contains(px, py);
    }

    @Override
    public TextLayer copy() {
        return new TextLayer(this);
    }

    @Override
    public String typeKey() {
        return "layer.text";
    }
}
