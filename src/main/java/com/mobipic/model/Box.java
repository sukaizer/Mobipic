package com.mobipic.model;

import javafx.geometry.Point2D;

import java.util.List;

/**
 * An axis-aligned box with a non-negative size, used by box-shaped layers for drawing and resizing.
 */
public record Box(double x, double y, double w, double h) {

    /**
     * Builds the box spanned by an anchor and an opposite point, whichever direction the drag goes.
     *
     * @param uniform force a square box (side = the largest of both deltas)
     */
    public static Box fromCorners(double ax, double ay, double bx, double by, boolean uniform) {
        double dx = bx - ax;
        double dy = by - ay;
        if (uniform) {
            double side = Math.max(Math.abs(dx), Math.abs(dy));
            dx = Math.copySign(side, dx);
            dy = Math.copySign(side, dy);
        }
        return new Box(Math.min(ax, ax + dx), Math.min(ay, ay + dy), Math.abs(dx), Math.abs(dy));
    }

    /** @return the corners in order: top-left, top-right, bottom-right, bottom-left */
    public List<Point2D> corners() {
        return List.of(
                new Point2D(x, y),
                new Point2D(x + w, y),
                new Point2D(x + w, y + h),
                new Point2D(x, y + h));
    }

    /** @return the box obtained by dragging corner {@code index} to the point, the opposite corner staying fixed */
    public Box withCornerMoved(int index, double px, double py, boolean uniform) {
        Point2D opposite = corners().get((index + 2) % 4);
        return fromCorners(opposite.getX(), opposite.getY(), px, py, uniform);
    }

    /**
     * @return the index, in {@code after}, of the corner that was dragged from corner {@code index} of
     * {@code before}: the one opposite to the fixed anchor (corners are renumbered when the box flips)
     */
    public static int draggedCorner(Box before, int index, Box after) {
        Point2D anchor = before.corners().get((index + 2) % 4);
        return (after.nearestCorner(anchor.getX(), anchor.getY()) + 2) % 4;
    }

    /** @return the index of the corner closest to the point */
    public int nearestCorner(double px, double py) {
        List<Point2D> corners = corners();
        int best = 0;
        for (int i = 1; i < corners.size(); i++) {
            if (corners.get(i).distance(px, py) < corners.get(best).distance(px, py)) {
                best = i;
            }
        }
        return best;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }
}
