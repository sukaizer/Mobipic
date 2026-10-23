package com.mobipic.model;

/**
 * Small geometry helpers for hit testing.
 */
final class Geometry {

    private Geometry() {
    }

    /** @return the distance from point P to the segment [AB] */
    static double distanceToSegment(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        t = Math.clamp(t, 0, 1);
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    /** @return the signed area (times 2) of the triangle ABC: positive if counter-clockwise */
    static double cross(double ax, double ay, double bx, double by, double cx, double cy) {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
    }
}
