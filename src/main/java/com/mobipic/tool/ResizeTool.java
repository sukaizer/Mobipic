package com.mobipic.tool;

import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.render.CanvasRenderer;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * Resizes the selected layer by dragging its handles (box corners, line ends, triangle vertices).
 */
public final class ResizeTool implements Tool {

    private int activeHandle = -1;
    private boolean resized;

    @Override
    public void onPress(ToolContext ctx, double x, double y) {
        activeHandle = handleAt(ctx, x, y);
        resized = false;
        if (activeHandle < 0) {
            ctx.model().select(ctx.model().layerAt(x, y));
        }
    }

    @Override
    public void onDrag(ToolContext ctx, double x, double y) {
        Layer layer = resizableSelection(ctx.model());
        if (activeHandle < 0 || layer == null) {
            return;
        }
        if (!resized) {
            ctx.model().checkpoint();
            resized = true;
        }
        activeHandle = layer.moveHandle(activeHandle, x, y);
        ctx.model().changed();
    }

    @Override
    public void onRelease(ToolContext ctx, double x, double y) {
        activeHandle = -1;
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        return activeHandle >= 0 || handleAt(ctx, x, y) >= 0 ? Cursor.CROSSHAIR : Cursor.DEFAULT;
    }

    @Override
    public void paintOverlay(ToolContext ctx, GraphicsContext gc) {
        Layer layer = resizableSelection(ctx.model());
        if (layer != null) {
            CanvasRenderer.paintHandles(gc, layer, ctx.zoom());
        }
    }

    private int handleAt(ToolContext ctx, double x, double y) {
        Layer layer = resizableSelection(ctx.model());
        if (layer == null) {
            return -1;
        }
        double tolerance = CanvasRenderer.HANDLE_SIZE / ctx.zoom();
        List<Point2D> handles = layer.handles();
        for (int i = 0; i < handles.size(); i++) {
            if (handles.get(i).distance(x, y) <= tolerance) {
                return i;
            }
        }
        return -1;
    }

    private static Layer resizableSelection(ProjectModel model) {
        Layer layer = model.getSelected();
        return model.isEditable(layer) && layer.isResizable() ? layer : null;
    }

    @Override
    public String hintKey() {
        return "tool.resize";
    }
}
