package com.mobipic.tool;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * A mouse event in canvas coordinates, with the modifiers tools care about.
 *
 * @param shift     keep proportions / snap angles
 * @param alt       draw from the centre
 * @param secondary right button
 */
public record ToolEvent(double x, double y, boolean shift, boolean alt, boolean secondary, int clickCount) {

    /**
     * @param scaleX canvas pixels per image pixel horizontally (the canvas may be rendered at another resolution)
     * @param scaleY canvas pixels per image pixel vertically
     */
    public static ToolEvent of(MouseEvent e, double scaleX, double scaleY) {
        return new ToolEvent(e.getX() / scaleX, e.getY() / scaleY, e.isShiftDown(), e.isAltDown(),
                e.getButton() == MouseButton.SECONDARY, e.getClickCount());
    }

    /** A plain left click or drag at the given point, handy for tests. */
    public static ToolEvent at(double x, double y) {
        return new ToolEvent(x, y, false, false, false, 1);
    }
}
