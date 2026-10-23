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

    private static final ShapeStyle STYLE = new ShapeStyle(Color.RED, Color.BLUE, 2, FillMode.OUTLINE);
    private static final double EPS = 1e-9;

    @Test
    void ellipseHitTest() {
        EllipseLayer ellipse = new EllipseLayer(0, 0, 100, 20, STYLE);
        assertTrue(ellipse.hitTest(50, 10));
        assertTrue(ellipse.hitTest(95, 10));
        assertFalse(ellipse.hitTest(50, 25));
        assertFalse(ellipse.hitTest(2, 2)); // bounding box corner
        assertFalse(new EllipseLayer(0, 0, 0, 0, STYLE).hitTest(0, 0));
    }

    @Test
    void lineHitTestFollowsTheSegmentNotItsBoundingBox() {
        LineLayer line = new LineLayer(0, 0, 100, 100, STYLE);
        assertTrue(line.hitTest(50, 50));
        assertTrue(line.hitTest(52, 50)); // within tolerance
        assertFalse(line.hitTest(90, 10));
    }

    @Test
    void triangleHitTestIncludesEdgesAndBothOrientations() {
        for (TriangleLayer t : List.of(new TriangleLayer(0, 0, 100, 0, 0, 100, STYLE),
                new TriangleLayer(0, 0, 0, 100, 100, 0, STYLE))) {
            assertTrue(t.hitTest(20, 20));
            assertTrue(t.hitTest(50, 0));
            assertFalse(t.hitTest(80, 80));
        }
        assertFalse(new TriangleLayer(0, 0, 50, 0, 100, 0, STYLE).hitTest(30, 50));
    }

    @Test
    void hiddenLayersCannotBeHit() {
        RectangleLayer rect = new RectangleLayer(0, 0, 10, 10, STYLE);
        rect.setVisible(false);
        assertFalse(rect.hitTest(5, 5));
    }

    @Test
    void rectangleResizeFromEveryHandle() {
        RectangleLayer rect = new RectangleLayer(0, 0, 100, 50, STYLE);
        rect.dragHandle(4, 150, 100, false);
        assertEquals(new Box(0, 0, 150, 100), rect.bounds());
        rect.dragHandle(3, 120, 0, false); // right edge
        assertEquals(new Box(0, 0, 120, 100), rect.bounds());
        rect.dragHandle(0, 60, 50, true); // top-left keeping the ratio
        assertEquals(1.2, rect.getWidth() / rect.getHeight(), EPS);
    }

    @Test
    void roundedRectangleKeepsItsRadiusAndType() {
        RectangleLayer rounded = new RectangleLayer(0, 0, 100, 50, 12, STYLE);
        assertEquals("layer.roundedRectangle", rounded.typeKey());
        assertEquals(12, rounded.copy().getCornerRadius());
        assertEquals("layer.square", new RectangleLayer(0, 0, 10, 10, STYLE).typeKey());
    }

    @Test
    void rotatedRectangleHitTestUsesItsRotatedShape() {
        RectangleLayer rect = new RectangleLayer(0, 40, 100, 20, STYLE); // centre (50, 50)
        rect.setRotation(90);
        assertTrue(rect.hitTest(50, 5)); // now vertical
        assertFalse(rect.hitTest(5, 50));
    }

    @Test
    void resizingARotatedLayerKeepsTheOppositeCornerOnScreen() {
        RectangleLayer rect = new RectangleLayer(0, 0, 100, 50, STYLE);
        rect.setRotation(30);
        Point2D anchorLocal = rect.handles().get(0);
        Point2D anchorBefore = rect.toWorld(anchorLocal.getX(), anchorLocal.getY());
        Point2D dragTo = rect.toWorld(160, 90);
        rect.dragHandle(4, dragTo.getX(), dragTo.getY(), false);
        Point2D anchorAfterLocal = rect.handles().get(0);
        Point2D anchorAfter = rect.toWorld(anchorAfterLocal.getX(), anchorAfterLocal.getY());
        assertEquals(anchorBefore.getX(), anchorAfter.getX(), 1e-6);
        assertEquals(anchorBefore.getY(), anchorAfter.getY(), 1e-6);
        assertEquals(160, rect.getWidth(), 1e-6);
        assertEquals(90, rect.getHeight(), 1e-6);
    }

    @Test
    void pointShapesRotateTheirPoints() {
        LineLayer line = new LineLayer(0, 0, 10, 0, STYLE);
        line.rotateBy(90, 0, 0);
        assertEquals(0, line.getRotation());
        assertEquals(0, line.getX2(), EPS);
        assertEquals(10, line.getY2(), EPS);
    }

    @Test
    void boxShapesRotateAroundAnyPivot() {
        RectangleLayer rect = new RectangleLayer(0, 0, 10, 10, STYLE); // centre (5, 5)
        rect.rotateBy(180, 0, 0);
        assertEquals(180, rect.getRotation());
        assertEquals(-5, rect.center().getX(), EPS);
        assertEquals(-5, rect.center().getY(), EPS);
        rect.setRotation(-90);
        assertEquals(270, rect.getRotation());
    }

    @Test
    void lineResizeCanSnapTo45Degrees() {
        LineLayer line = new LineLayer(0, 0, 10, 0, STYLE);
        line.dragHandle(1, 100, 90, true);
        assertEquals(line.getX2(), line.getY2(), 1e-6);
    }

    @Test
    void translateMovesEveryPoint() {
        TriangleLayer t = new TriangleLayer(0, 0, 10, 0, 0, 10, STYLE);
        t.translate(5, 7);
        assertEquals(List.of(new Point2D(5, 7), new Point2D(15, 7), new Point2D(5, 17)), t.handles());
    }

    @Test
    void copiesAreIndependentAndKeepCommonProperties() {
        RectangleLayer original = new RectangleLayer(0, 0, 10, 10, STYLE);
        original.setOpacity(0.5);
        original.setRotation(45);
        original.setName("Logo");
        original.setVisible(false);
        RectangleLayer copy = original.copy();
        assertNotSame(original, copy);
        assertEquals(0.5, copy.getOpacity());
        assertEquals(45, copy.getRotation());
        assertEquals("Logo", copy.getName());
        assertFalse(copy.isVisible());
        copy.translate(5, 5);
        copy.dragHandle(4, 100, 100, false);
        assertEquals(0, original.getX());
        assertEquals(10, original.getWidth());
    }

    @Test
    void openShapesAreNeverFilled() {
        ShapeStyle filled = STYLE.withFillMode(FillMode.OUTLINE_AND_FILL);
        assertEquals(FillMode.OUTLINE, new LineLayer(0, 0, 1, 1, filled).getStyle().fillMode());
        assertEquals(FillMode.OUTLINE, new PathLayer(0, 0, filled).getStyle().fillMode());
        assertEquals(FillMode.OUTLINE_AND_FILL, new EllipseLayer(0, 0, 1, 1, filled).getStyle().fillMode());
    }

    @Test
    void blankNamesAreCleared() {
        RectangleLayer rect = new RectangleLayer(0, 0, 1, 1, STYLE);
        rect.setName("  ");
        assertEquals(null, rect.getName());
        rect.setName("  Fond ");
        assertEquals("Fond", rect.getName());
    }

    @Test
    void opacityIsClamped() {
        RectangleLayer rect = new RectangleLayer(0, 0, 1, 1, STYLE);
        rect.setOpacity(3);
        assertEquals(1, rect.getOpacity());
        rect.setOpacity(-1);
        assertEquals(0, rect.getOpacity());
    }
}
