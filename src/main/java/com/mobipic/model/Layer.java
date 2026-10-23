package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * A drawable element of a project. Layers are painted in list order, the first one being the base image.
 * <p>
 * Layers hold geometry and style only: no UI state, no reference to a canvas.
 */
public abstract sealed class Layer permits ShapeLayer, ImageLayer, TextLayer {

    protected double x;
    protected double y;

    protected Layer(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /** Moves the whole layer by the given offset. */
    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /** Paints the layer with its own style. */
    public abstract void paint(GraphicsContext gc);

    /** Strokes the layer outline using the stroke settings currently set on {@code gc}. */
    public abstract void strokeOutline(GraphicsContext gc);

    /** @return whether the point (in canvas coordinates) hits this layer */
    public abstract boolean contains(double px, double py);

    /** @return the control points that can be dragged to resize the layer (empty if not resizable) */
    public List<Point2D> handles() {
        return List.of();
    }

    /**
     * Drags the handle {@code index} to the given point.
     *
     * @return the index of the dragged handle after the move (corners may swap when a box is flipped)
     */
    public int moveHandle(int index, double px, double py) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not resizable");
    }

    public boolean isResizable() {
        return !handles().isEmpty();
    }

    /** @return a deep, independent copy of this layer */
    public abstract Layer copy();

    /** @return the i18n key of the layer type name */
    public abstract String typeKey();
}
