package com.mobipic.tool;

import com.mobipic.model.FillMode;
import com.mobipic.model.PathLayer;
import javafx.scene.Cursor;

/**
 * Freehand drawing with the primary colour. Each stroke becomes a layer that can be moved, resized and rotated.
 */
public final class BrushTool implements Tool {

    /** Minimum distance between recorded points, in screen pixels, to keep strokes light and smooth. */
    private static final double MIN_STEP = 1.5;

    private PathLayer stroke;

    @Override
    public void onPress(ToolContext ctx, ToolEvent e) {
        ToolSettings s = ctx.settings();
        stroke = new PathLayer(e.x(), e.y(), s.shapeStyle().withFillMode(FillMode.OUTLINE));
        stroke.setOpacity(s.opacityProperty().get());
        ctx.model().add(stroke);
    }

    @Override
    public void onDrag(ToolContext ctx, ToolEvent e) {
        if (stroke != null && stroke.lastPoint().distance(e.x(), e.y()) * ctx.zoom() >= MIN_STEP) {
            stroke.addPoint(e.x(), e.y());
            ctx.model().changed();
        }
    }

    @Override
    public void onRelease(ToolContext ctx, ToolEvent e) {
        if (stroke != null && !stroke.lastPoint().equals(new javafx.geometry.Point2D(e.x(), e.y()))) {
            stroke.addPoint(e.x(), e.y());
            ctx.model().changed();
        }
        stroke = null;
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        return Cursor.CROSSHAIR;
    }

    @Override
    public String hintKey() {
        return "tool.hint.brush";
    }
}
