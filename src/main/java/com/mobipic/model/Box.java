package com.mobipic.model;

import javafx.geometry.Point2D;

import java.util.List;

/**
 * An axis-aligned box with a non-negative size, used to draw and resize layers.
 * <p>
 * Resize handles are numbered clockwise from the top-left corner:
 * 0 top-left, 1 top, 2 top-right, 3 right, 4 bottom-right, 5 bottom, 6 bottom-left, 7 left.
 * Even indices are corners, odd indices are edges.
 */
public record Box(double x, double y, double w, double h) {

    public static final int HANDLE_COUNT = 8;

    /**
     * Builds the box spanned by an anchor and an opposite point, whichever direction the drag goes.
     *
     * @param ratio width / height ratio to keep, or {@code 0} for a free box
     */
    public static Box fromCorners(double ax, double ay, double bx, double by, double ratio) {
        double dx = bx - ax;
        double dy = by - ay;
        if (ratio > 0) {
            double scale = Math.max(Math.abs(dx) / ratio, Math.abs(dy));
            dx = Math.copySign(scale * ratio, dx);
            dy = Math.copySign(scale, dy);
        }
        return new Box(Math.min(ax, ax + dx), Math.min(ay, ay + dy), Math.abs(dx), Math.abs(dy));
    }

    /** Builds the box centred on {@code (cx, cy)} with a corner at {@code (bx, by)}. */
    public static Box fromCenter(double cx, double cy, double bx, double by, double ratio) {
        Box half = fromCorners(cx, cy, bx, by, ratio);
        return new Box(cx - half.w, cy - half.h, 2 * half.w, 2 * half.h);
    }

    public double maxX() {
        return x + w;
    }

    public double maxY() {
        return y + h;
    }

    public Point2D center() {
        return new Point2D(x + w / 2, y + h / 2);
    }

    /** @return the 8 resize handles, see the class documentation for their order */
    public List<Point2D> handles() {
        double cx = x + w / 2;
        double cy = y + h / 2;
        return List.of(
                new Point2D(x, y), new Point2D(cx, y), new Point2D(maxX(), y), new Point2D(maxX(), cy),
                new Point2D(maxX(), maxY()), new Point2D(cx, maxY()), new Point2D(x, maxY()), new Point2D(x, cy));
    }

    /** @return the four corners: top-left, top-right, bottom-right, bottom-left */
    public List<Point2D> corners() {
        return List.of(new Point2D(x, y), new Point2D(maxX(), y), new Point2D(maxX(), maxY()), new Point2D(x, maxY()));
    }

    public static int oppositeHandle(int index) {
        return (index + 4) % HANDLE_COUNT;
    }

    /**
     * @return the box obtained by dragging handle {@code index} to the point, the opposite handle staying fixed
     * @param keepRatio for corners, keep the current width / height ratio
     */
    public Box withHandleMoved(int index, double px, double py, boolean keepRatio) {
        return switch (index) {
            case 0, 2, 4, 6 -> {
                Point2D anchor = handles().get(oppositeHandle(index));
                double ratio = keepRatio && w > 0 && h > 0 ? w / h : 0;
                yield fromCorners(anchor.getX(), anchor.getY(), px, py, ratio);
            }
            case 1 -> vertical(maxY(), py);
            case 5 -> vertical(y, py);
            case 3 -> horizontal(x, px);
            case 7 -> horizontal(maxX(), px);
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    private Box vertical(double fixedY, double py) {
        return new Box(x, Math.min(fixedY, py), w, Math.abs(py - fixedY));
    }

    private Box horizontal(double fixedX, double px) {
        return new Box(Math.min(fixedX, px), y, Math.abs(px - fixedX), h);
    }

    /**
     * @return the index, in {@code after}, of the handle dragged from {@code index} in {@code before}
     * (handles are renumbered when the box flips): the one opposite to the fixed anchor
     */
    public static int draggedHandle(Box before, int index, Box after) {
        Point2D anchor = before.handles().get(oppositeHandle(index));
        List<Point2D> handles = after.handles();
        int best = -1;
        for (int i = index % 2; i < HANDLE_COUNT; i += 2) { // same kind: corner or edge
            if (best < 0 || handles.get(i).distance(anchor) < handles.get(best).distance(anchor)) {
                best = i;
            }
        }
        return oppositeHandle(best);
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= maxX() && py >= y && py <= maxY();
    }

    /** @return the smallest box containing all the points */
    public static Box around(double[] xs, double[] ys) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < xs.length; i++) {
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxY = Math.max(maxY, ys[i]);
        }
        return xs.length == 0 ? new Box(0, 0, 0, 0) : new Box(minX, minY, maxX - minX, maxY - minY);
    }
}
