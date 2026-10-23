package com.mobipic.model;

import javafx.geometry.Point2D;

/**
 * Small geometry helpers for hit testing and constrained drawing.
 */
public final class Geometry {

    /** Angle step used when Shift constrains a line or a rotation. */
    public static final double SNAP_DEGREES = 15;

    private Geometry() {
    }

    /** @return the distance from point P to the segment [AB] */
    public static double distanceToSegment(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        t = Math.clamp(t, 0, 1);
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    /** @return the signed area (times 2) of the triangle ABC */
    static double cross(double ax, double ay, double bx, double by, double cx, double cy) {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
    }

    /** @return the point P moved so that the segment from the origin to it is a multiple of 45 degrees */
    public static Point2D snapAngle(double ox, double oy, double px, double py) {
        double length = Math.hypot(px - ox, py - oy);
        double angle = Math.round(Math.atan2(py - oy, px - ox) / (Math.PI / 4)) * (Math.PI / 4);
        return new Point2D(ox + length * Math.cos(angle), oy + length * Math.sin(angle));
    }

    /** @return the angle in degrees of the vector from the origin to P, clockwise on screen */
    public static double angle(double ox, double oy, double px, double py) {
        return Math.toDegrees(Math.atan2(py - oy, px - ox));
    }
}
