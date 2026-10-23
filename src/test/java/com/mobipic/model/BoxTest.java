package com.mobipic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxTest {

    @Test
    void fromCornersWorksInEveryDirection() {
        Box expected = new Box(10, 20, 30, 40);
        assertEquals(expected, Box.fromCorners(10, 20, 40, 60, 0));
        assertEquals(expected, Box.fromCorners(40, 60, 10, 20, 0));
        assertEquals(expected, Box.fromCorners(40, 20, 10, 60, 0));
        assertEquals(expected, Box.fromCorners(10, 60, 40, 20, 0));
    }

    @Test
    void ratioIsKept() {
        assertEquals(new Box(10, 10, 50, 50), Box.fromCorners(10, 10, 30, 60, 1));
        // Dragging up-left keeps the anchor as the bottom-right corner
        assertEquals(new Box(-20, 10, 30, 30), Box.fromCorners(10, 40, -20, 30, 1));
        assertEquals(new Box(0, 0, 40, 20), Box.fromCorners(0, 0, 40, 5, 2));
    }

    @Test
    void fromCenterIsSymmetric() {
        assertEquals(new Box(40, 30, 20, 40), Box.fromCenter(50, 50, 60, 70, 0));
    }

    @Test
    void cornerHandleKeepsTheOppositeCornerFixed() {
        Box box = new Box(0, 0, 100, 50);
        assertEquals(new Box(0, 0, 120, 80), box.withHandleMoved(4, 120, 80, false));
        // Drag top-left (0) past the bottom-right corner: the box flips
        Box flipped = box.withHandleMoved(0, 150, 70, false);
        assertEquals(new Box(100, 50, 50, 20), flipped);
        assertEquals(4, Box.draggedHandle(box, 0, flipped));
    }

    @Test
    void cornerHandleCanKeepTheRatio() {
        Box box = new Box(0, 0, 100, 50);
        assertEquals(new Box(0, 0, 200, 100), box.withHandleMoved(4, 200, 60, true));
    }

    @Test
    void edgeHandlesChangeOneDimension() {
        Box box = new Box(0, 0, 100, 50);
        assertEquals(new Box(0, 0, 100, 80), box.withHandleMoved(5, 999, 80, false)); // bottom
        assertEquals(new Box(30, 0, 70, 50), box.withHandleMoved(7, 30, 999, false)); // left
        Box flipped = box.withHandleMoved(1, 10, 70, false); // top dragged below the bottom
        assertEquals(new Box(0, 50, 100, 20), flipped);
        assertEquals(5, Box.draggedHandle(box, 1, flipped));
    }

    @Test
    void containsIsInclusive() {
        Box box = new Box(0, 0, 10, 10);
        assertTrue(box.contains(0, 0));
        assertTrue(box.contains(10, 10));
        assertFalse(box.contains(10.1, 5));
    }

    @Test
    void aroundPoints() {
        assertEquals(new Box(-1, 2, 6, 8), Box.around(new double[]{-1, 5, 0}, new double[]{2, 4, 10}));
    }
}
