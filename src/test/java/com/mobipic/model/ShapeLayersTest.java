package com.mobipic.model;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapeLayersTest {

    private static final ShapeStyle STYLE = new ShapeStyle(Color.RED, 2, false);

    @Test
    void rectangleDrawnUpLeftIsNormalized() {
        RectangleLayer rect = (RectangleLayer) ShapeType.RECTANGLE.create(100, 100, STYLE);
        rect.setFromDrag(100, 100, 40, 70);
        assertEquals(40, rect.getX());
        assertEquals(70, rect.getY());
        assertEquals(60, rect.getWidth());
        assertEquals(30, rect.getHeight());
    }

    @Test
    void circleRadiusUsesBothAxes() {
        CircleLayer circle = (CircleLayer) ShapeType.CIRCLE.create(0, 0, STYLE);
        circle.setFromDrag(0, 0, 10, 40);
        assertEquals(20, circle.getRadius());
        assertTrue(circle.contains(20, 20));
        assertFalse(circle.contains(1, 1)); // bounding box corner, outside the circle
    }

    @Test
    void ellipseHitTest() {
        EllipseLayer ellipse = new EllipseLayer(0, 0, 50, 10, STYLE);
        assertTrue(ellipse.contains(50, 10));
        assertTrue(ellipse.contains(95, 10));
        assertFalse(ellipse.contains(50, 25));
        assertFalse(new EllipseLayer(0, 0, 0, 0, STYLE).contains(0, 0));
    }

    @Test
    void lineHitTestFollowsTheSegmentNotItsBoundingBox() {
        LineLayer line = new LineLayer(0, 0, 100, 100, STYLE);
        assertTrue(line.contains(50, 50));
        assertTrue(line.contains(52, 50)); // within tolerance
        assertFalse(line.contains(90, 10)); // inside the bounding box, far from the line
    }

    @Test
    void triangleHitTestIncludesEdgesAndBothOrientations() {
        TriangleLayer clockwise = new TriangleLayer(0, 0, 100, 0, 0, 100, STYLE);
        TriangleLayer counterClockwise = new TriangleLayer(0, 0, 0, 100, 100, 0, STYLE);
        for (TriangleLayer t : List.of(clockwise, counterClockwise)) {
            assertTrue(t.contains(20, 20));
            assertTrue(t.contains(50, 0)); // on an edge
            assertFalse(t.contains(80, 80));
        }
    }

    @Test
    void degenerateTriangleOnlyHitsItsEdges() {
        TriangleLayer flat = new TriangleLayer(0, 0, 50, 0, 100, 0, STYLE);
        assertTrue(flat.contains(30, 0));
        assertFalse(flat.contains(30, 50));
    }

    @Test
    void translateMovesEveryPoint() {
        TriangleLayer t = new TriangleLayer(0, 0, 10, 0, 0, 10, STYLE);
        t.translate(5, 7);
        assertEquals(List.of(new Point2D(5, 7), new Point2D(15, 7), new Point2D(5, 17)), t.handles());

        LineLayer line = new LineLayer(0, 0, 10, 10, STYLE);
        line.translate(-1, 1);
        assertEquals(List.of(new Point2D(-1, 1), new Point2D(9, 11)), line.handles());
    }

    @Test
    void squareResizeStaysSquare() {
        SquareLayer square = new SquareLayer(0, 0, 10, STYLE);
        int handle = square.moveHandle(2, 30, 15);
        assertEquals(30, square.getSide());
        assertEquals(2, handle);
    }

    @Test
    void lineResizeMovesOneEnd() {
        LineLayer line = new LineLayer(0, 0, 10, 10, STYLE);
        line.moveHandle(1, 50, 60);
        assertEquals(0, line.getX());
        assertEquals(50, line.getX2());
        assertEquals(60, line.getY2());
    }

    @Test
    void copiesAreIndependent() {
        RectangleLayer original = new RectangleLayer(0, 0, 10, 10, STYLE);
        RectangleLayer copy = original.copy();
        assertNotSame(original, copy);
        copy.translate(5, 5);
        copy.moveHandle(2, 100, 100);
        assertEquals(0, original.getX());
        assertEquals(10, original.getWidth());
        assertEquals(STYLE, copy.getStyle());
    }

    @Test
    void linesAreNeverFilled() {
        LineLayer line = new LineLayer(0, 0, 1, 1, new ShapeStyle(Color.BLACK, 1, true));
        assertFalse(line.isFilled());
    }
}
