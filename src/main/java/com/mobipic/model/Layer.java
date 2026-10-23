package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * A drawable element of a project. Layers are painted in list order, the first one being the base image.
 * <p>
 * Geometry is expressed in <em>local</em> coordinates. Box-shaped layers can also carry a rotation
 * (in degrees, around the centre of their {@link #bounds() bounds}); point-based layers (lines, triangles,
 * brush strokes) are rotated by moving their points instead. {@code world} coordinates are canvas coordinates.
 * <p>
 * Layers hold geometry and style only: no UI state, no reference to a canvas.
 */
public abstract sealed class Layer permits ShapeLayer, ImageLayer, TextLayer {

    protected double x;
    protected double y;
    private double rotation;
    private double opacity = 1;
    private boolean visible = true;
    private String name;

    protected Layer(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Copies the properties shared by all layers. */
    protected Layer(Layer other) {
        this(other.x, other.y);
        this.rotation = other.rotation;
        this.opacity = other.opacity;
        this.visible = other.visible;
        this.name = other.name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // ---- Common properties ------------------------------------------------------------------

    /** @return the rotation in degrees, in [0, 360) */
    public double getRotation() {
        return rotation;
    }

    public void setRotation(double degrees) {
        if (!usesRotation()) {
            return;
        }
        double normalized = degrees % 360;
        rotation = normalized < 0 ? normalized + 360 : normalized;
    }

    /** @return whether the layer is rotated through {@link #getRotation()}, rather than by moving its points */
    protected boolean usesRotation() {
        return true;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = Math.clamp(opacity, 0, 1);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /** @return the name given by the user, or {@code null} to use the type name */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null || name.isBlank() ? null : name.strip();
    }

    // ---- Geometry ---------------------------------------------------------------------------

    /** @return the unrotated bounding box, in local coordinates */
    public abstract Box bounds();

    /** @return the rotation pivot: the centre of the bounds */
    public Point2D center() {
        return bounds().center();
    }

    /** Converts canvas coordinates to the layer's unrotated frame. */
    public Point2D toLocal(double wx, double wy) {
        if (rotation == 0) {
            return new Point2D(wx, wy);
        }
        Point2D c = center();
        return new Rotate(-rotation, c.getX(), c.getY()).transform(wx, wy);
    }

    /** Converts a point of the layer's unrotated frame to canvas coordinates. */
    public Point2D toWorld(double lx, double ly) {
        if (rotation == 0) {
            return new Point2D(lx, ly);
        }
        Point2D c = center();
        return new Rotate(rotation, c.getX(), c.getY()).transform(lx, ly);
    }

    /** Moves the whole layer by the given offset. */
    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /** Rotates the layer by {@code degrees} (clockwise) around the given canvas point. */
    public void rotateBy(double degrees, double px, double py) {
        Point2D c = center();
        Point2D moved = new Rotate(degrees, px, py).transform(c);
        translate(moved.getX() - c.getX(), moved.getY() - c.getY());
        setRotation(rotation + degrees);
    }

    // ---- Painting and hit testing -----------------------------------------------------------

    /** Paints the layer in local coordinates (opacity and rotation are applied by the caller). */
    public abstract void paint(GraphicsContext gc);

    /** Strokes the selection outline, in local coordinates, using the current stroke settings of {@code gc}. */
    public void strokeOutline(GraphicsContext gc) {
        Box b = bounds();
        gc.strokeRect(b.x(), b.y(), b.w(), b.h());
    }

    /** @return whether the point, in local coordinates, hits the layer */
    protected abstract boolean contains(double lx, double ly);

    /** @return whether a visible part of the layer is under the canvas point */
    public boolean hitTest(double wx, double wy) {
        if (!visible) {
            return false;
        }
        Point2D p = toLocal(wx, wy);
        return contains(p.getX(), p.getY());
    }

    // ---- Resizing ---------------------------------------------------------------------------

    /** @return the resize handles in local coordinates (empty if not resizable) */
    public List<Point2D> handles() {
        return List.of();
    }

    /** @return the index of the handle that stays fixed while {@code index} is dragged, or {@code -1} */
    protected int oppositeHandle(int index) {
        return -1;
    }

    /**
     * Drags a handle to a point in local coordinates.
     *
     * @param keepRatio keep the proportions (Shift)
     * @return the index of the dragged handle after the move (handles are renumbered when a box flips)
     */
    protected int moveHandle(int index, double lx, double ly, boolean keepRatio) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not resizable");
    }

    /**
     * Drags a handle to a canvas point. For a rotated layer, the opposite handle stays where it is on screen.
     *
     * @return the index of the dragged handle after the move
     */
    public final int dragHandle(int index, double wx, double wy, boolean keepRatio) {
        int opposite = oppositeHandle(index);
        if (rotation == 0 || opposite < 0) {
            Point2D p = toLocal(wx, wy);
            return moveHandle(index, p.getX(), p.getY(), keepRatio);
        }
        Point2D anchorLocal = handles().get(opposite);
        Point2D anchorBefore = toWorld(anchorLocal.getX(), anchorLocal.getY());
        Point2D p = toLocal(wx, wy);
        int result = moveHandle(index, p.getX(), p.getY(), keepRatio);
        // Resizing moved the rotation pivot: shift back so the anchor does not move on screen
        Point2D anchorAfter = toWorld(anchorLocal.getX(), anchorLocal.getY());
        translate(anchorBefore.getX() - anchorAfter.getX(), anchorBefore.getY() - anchorAfter.getY());
        return result;
    }

    public boolean isResizable() {
        return !handles().isEmpty();
    }

    // ---- Misc -------------------------------------------------------------------------------

    /** @return a deep, independent copy of this layer */
    public abstract Layer copy();

    /** @return the i18n key of the layer type name */
    public abstract String typeKey();
}
