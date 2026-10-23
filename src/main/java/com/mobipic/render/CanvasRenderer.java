package com.mobipic.render;

import com.mobipic.model.Box;
import com.mobipic.model.Layer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Paints layers and editing overlays (selection frame, resize and rotation handles) on a canvas.
 */
public final class CanvasRenderer {

    /** Size of a resize handle, in screen pixels. */
    public static final double HANDLE_SIZE = 8;
    /** Distance between the top of the selection frame and the rotation handle, in screen pixels. */
    public static final double ROTATION_HANDLE_DISTANCE = 24;

    private CanvasRenderer() {
    }

    /** Clears the canvas and paints the visible layers in order. */
    public static void paintLayers(Canvas canvas, List<? extends Layer> layers) {
        paintLayers(canvas, layers, null);
    }

    /**
     * Clears the canvas and paints the visible layers in order.
     *
     * @param skipped a layer not to paint (e.g. a text being edited on top of the canvas), or {@code null}
     */
    public static void paintLayers(Canvas canvas, List<? extends Layer> layers, Layer skipped) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Layer layer : layers) {
            if (layer.isVisible() && layer != skipped) {
                paintLayer(gc, layer);
            }
        }
    }

    /** Paints one layer with its opacity and rotation, leaving the graphics state untouched. */
    public static void paintLayer(GraphicsContext gc, Layer layer) {
        gc.save();
        gc.setGlobalAlpha(layer.getOpacity());
        applyRotation(gc, layer);
        layer.paint(gc);
        gc.restore();
    }

    private static void applyRotation(GraphicsContext gc, Layer layer) {
        if (layer.getRotation() != 0) {
            Point2D c = layer.center();
            gc.translate(c.getX(), c.getY());
            gc.rotate(layer.getRotation());
            gc.translate(-c.getX(), -c.getY());
        }
    }

    /**
     * Outlines the selected layer: a dark line under a light dashed line, readable on any background.
     *
     * @param zoom the current zoom factor, so the outline keeps the same on-screen width
     */
    public static void paintSelection(GraphicsContext gc, Layer layer, double zoom) {
        gc.save();
        applyRotation(gc, layer);
        gc.setStroke(Color.rgb(0, 0, 0, 0.85));
        gc.setLineWidth(3 / zoom);
        layer.strokeOutline(gc);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1 / zoom);
        gc.setLineDashes(5 / zoom, 4 / zoom);
        layer.strokeOutline(gc);
        gc.restore();
    }

    /** Paints the resize handles and the rotation handle of a layer. */
    public static void paintHandles(GraphicsContext gc, Layer layer, double zoom) {
        double size = HANDLE_SIZE / zoom;
        gc.save();
        gc.setLineWidth(1 / zoom);

        Point2D top = rotationHandleBase(layer);
        Point2D knob = rotationHandle(layer, zoom);
        gc.setStroke(Color.rgb(0, 0, 0, 0.7));
        gc.strokeLine(top.getX(), top.getY(), knob.getX(), knob.getY());
        gc.setFill(Color.WHITE);
        gc.fillOval(knob.getX() - size / 2, knob.getY() - size / 2, size, size);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(knob.getX() - size / 2, knob.getY() - size / 2, size, size);

        for (Point2D local : layer.handles()) {
            Point2D handle = layer.toWorld(local.getX(), local.getY());
            double hx = handle.getX() - size / 2;
            double hy = handle.getY() - size / 2;
            gc.setFill(Color.WHITE);
            gc.fillRect(hx, hy, size, size);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(hx, hy, size, size);
        }
        gc.restore();
    }

    /** @return the middle of the top edge of the layer frame, in canvas coordinates */
    private static Point2D rotationHandleBase(Layer layer) {
        Box b = layer.bounds();
        return layer.toWorld(b.x() + b.w() / 2, b.y());
    }

    /** @return the position of the rotation handle, above the layer frame, in canvas coordinates */
    public static Point2D rotationHandle(Layer layer, double zoom) {
        Box b = layer.bounds();
        return layer.toWorld(b.x() + b.w() / 2, b.y() - ROTATION_HANDLE_DISTANCE / zoom);
    }
}
