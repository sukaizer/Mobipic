package com.mobipic.tool;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;

/**
 * A canvas interaction mode. Coordinates are canvas coordinates (independent of the zoom).
 */
public interface Tool {

    default void onPress(ToolContext ctx, double x, double y) {
    }

    default void onDrag(ToolContext ctx, double x, double y) {
    }

    default void onRelease(ToolContext ctx, double x, double y) {
    }

    default void onMove(ToolContext ctx, double x, double y) {
    }

    /** Aborts any operation in progress. */
    default void cancel(ToolContext ctx) {
    }

    default Cursor cursorAt(ToolContext ctx, double x, double y) {
        return Cursor.DEFAULT;
    }

    /** Paints tool-specific feedback on top of the layers. */
    default void paintOverlay(ToolContext ctx, GraphicsContext gc) {
    }

    /** @return the i18n key of the short help shown while the tool is active */
    String hintKey();
}
