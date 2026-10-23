package com.mobipic.render;

import com.mobipic.model.Layer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Paints layers and editing overlays (selection outline, resize handles) on a canvas.
 */
public final class CanvasRenderer {

    /** Size of a resize handle, in screen pixels. */
    public static final double HANDLE_SIZE = 8;

    private CanvasRenderer() {
    }

    /** Clears the canvas and paints the layers in order, each with its own graphics state. */
    public static void paintLayers(Canvas canvas, List<? extends Layer> layers) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Layer layer : layers) {
            gc.save();
            layer.paint(gc);
            gc.restore();
        }
    }

    /**
     * Outlines the selected layer: a dark line under a light dashed line, readable on any background.
     *
     * @param zoom the current zoom factor, so the outline keeps the same on-screen width
     */
    public static void paintSelection(GraphicsContext gc, Layer layer, double zoom) {
        gc.save();
        gc.setStroke(Color.rgb(0, 0, 0, 0.85));
        gc.setLineWidth(3 / zoom);
        layer.strokeOutline(gc);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1 / zoom);
        gc.setLineDashes(5 / zoom, 4 / zoom);
        layer.strokeOutline(gc);
        gc.restore();
    }

    /** Paints the resize handles of a layer as small filled squares. */
    public static void paintHandles(GraphicsContext gc, Layer layer, double zoom) {
        double size = HANDLE_SIZE / zoom;
        gc.save();
        gc.setLineWidth(1 / zoom);
        for (Point2D handle : layer.handles()) {
            double x = handle.getX() - size / 2;
            double y = handle.getY() - size / 2;
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, size, size);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x, y, size, size);
        }
        gc.restore();
    }
}
