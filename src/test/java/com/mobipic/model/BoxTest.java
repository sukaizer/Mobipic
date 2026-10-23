package com.mobipic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxTest {

    @Test
    void fromCornersWorksInEveryDirection() {
        Box expected = new Box(10, 20, 30, 40);
        assertEquals(expected, Box.fromCorners(10, 20, 40, 60, false));
        assertEquals(expected, Box.fromCorners(40, 60, 10, 20, false));
        assertEquals(expected, Box.fromCorners(40, 20, 10, 60, false));
        assertEquals(expected, Box.fromCorners(10, 60, 40, 20, false));
    }

    @Test
    void uniformBoxUsesTheLargestDelta() {
        assertEquals(new Box(10, 10, 50, 50), Box.fromCorners(10, 10, 30, 60, true));
        // Dragging up-left keeps the anchor as the bottom-right corner
        assertEquals(new Box(-20, 10, 30, 30), Box.fromCorners(10, 40, -20, 30, true));
    }

    @Test
    void movingACornerKeepsTheOppositeOneFixed() {
        Box box = new Box(0, 0, 100, 50);
        // Drag bottom-right (index 2)
        assertEquals(new Box(0, 0, 120, 80), box.withCornerMoved(2, 120, 80, false));
        // Drag top-left (index 0) past the bottom-right corner: the box flips
        Box flipped = box.withCornerMoved(0, 150, 70, false);
        assertEquals(new Box(100, 50, 50, 20), flipped);
        assertEquals(2, Box.draggedCorner(box, 0, flipped));
    }

    @Test
    void containsIsInclusive() {
        Box box = new Box(0, 0, 10, 10);
        assertTrue(box.contains(0, 0));
        assertTrue(box.contains(10, 10));
        assertFalse(box.contains(10.1, 5));
    }
}
