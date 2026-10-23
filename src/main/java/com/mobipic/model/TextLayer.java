package com.mobipic.model;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Objects;

/**
 * A block of text, possibly on several lines, whose top-left corner is {@code (x, y)}.
 * Resizing a text layer scales its font size.
 */
public final class TextLayer extends Layer {

    /** Shadow offset, relative to the font size. */
    private static final double SHADOW_OFFSET = 0.06;
    private static final Color SHADOW_COLOR = Color.rgb(0, 0, 0, 0.45);

    private String text;
    private TextStyle style;

    /** Line layout derived from the text and style, computed lazily. */
    private String[] lines;
    private double[] lineWidths;
    private double lineHeight;
    private double blockWidth;

    public TextLayer(double x, double y, String text, TextStyle style) {
        super(x, y);
        this.text = Objects.requireNonNull(text, "text");
        this.style = Objects.requireNonNull(style, "style");
    }

    private TextLayer(TextLayer other) {
        super(other);
        this.text = other.text;
        this.style = other.style;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = Objects.requireNonNull(text, "text");
        invalidate();
    }

    public TextStyle getStyle() {
        return style;
    }

    public void setStyle(TextStyle style) {
        this.style = Objects.requireNonNull(style, "style");
        invalidate();
    }

    public Font getFont() {
        return style.font();
    }

    private void invalidate() {
        lines = null;
    }

    private void layout() {
        if (lines != null) {
            return;
        }
        Font font = style.font();
        lines = text.split("\n", -1);
        lineWidths = new double[lines.length];
        Text measure = new Text();
        measure.setFont(font);
        measure.setTextOrigin(VPos.TOP);
        blockWidth = 0;
        for (int i = 0; i < lines.length; i++) {
            measure.setText(lines[i]);
            lineWidths[i] = measure.getLayoutBounds().getWidth();
            blockWidth = Math.max(blockWidth, lineWidths[i]);
        }
        measure.setText("Ag");
        Bounds lineBounds = measure.getLayoutBounds();
        lineHeight = lineBounds.getHeight();
    }

    /** @return the height of one line of text, in canvas pixels */
    public double getLineHeight() {
        layout();
        return lineHeight;
    }

    @Override
    public Box bounds() {
        layout();
        return new Box(x, y, blockWidth, lineHeight * lines.length);
    }

    @Override
    public void paint(GraphicsContext gc) {
        layout();
        gc.setFont(style.font());
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        if (style.shadow()) {
            double offset = Math.max(1, style.size() * SHADOW_OFFSET);
            gc.setFill(SHADOW_COLOR);
            forEachLine((line, lx, ly) -> gc.fillText(line, lx + offset, ly + offset));
        }
        if (style.outlineWidth() > 0) {
            // Stroke first, then fill on top: the outline grows outwards and the letters stay readable
            gc.setStroke(style.outlineColor());
            gc.setLineWidth(style.outlineWidth() * 2);
            gc.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            forEachLine(gc::strokeText);
        }
        gc.setFill(style.color());
        forEachLine(gc::fillText);
    }

    private interface LineConsumer {
        void accept(String line, double x, double y);
    }

    private void forEachLine(LineConsumer consumer) {
        for (int i = 0; i < lines.length; i++) {
            double offset = switch (style.alignment()) {
                case CENTER -> (blockWidth - lineWidths[i]) / 2;
                case RIGHT -> blockWidth - lineWidths[i];
                default -> 0;
            };
            consumer.accept(lines[i], x + offset, y + i * lineHeight);
        }
    }

    @Override
    protected boolean contains(double lx, double ly) {
        return bounds().contains(lx, ly);
    }

    /** Text is resized from its corners only, always keeping its proportions. */
    @Override
    public List<Point2D> handles() {
        return bounds().corners();
    }

    @Override
    protected int oppositeHandle(int index) {
        return (index + 2) % 4;
    }

    @Override
    protected int moveHandle(int index, double lx, double ly, boolean keepRatio) {
        Box before = bounds();
        if (before.h() <= 0) {
            return index;
        }
        Point2D anchor = before.corners().get(oppositeHandle(index));
        Box target = Box.fromCorners(anchor.getX(), anchor.getY(), lx, ly,
                before.w() > 0 ? before.w() / before.h() : 1);
        double newSize = Math.max(1, style.size() * target.h() / before.h());
        setStyle(style.withSize(newSize));
        // Place the new block so that the anchor corner did not move
        Box after = bounds();
        boolean draggedLeft = lx < anchor.getX();
        boolean draggedUp = ly < anchor.getY();
        x = draggedLeft ? anchor.getX() - after.w() : anchor.getX();
        y = draggedUp ? anchor.getY() - after.h() : anchor.getY();
        // Index of the dragged corner in the new box (corners: 0 top-left, 1 top-right, 2 bottom-right, 3 bottom-left)
        int col = draggedLeft ? 0 : 1;
        return draggedUp ? col : 3 - col;
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
