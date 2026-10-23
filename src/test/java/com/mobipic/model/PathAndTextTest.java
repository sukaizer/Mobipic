package com.mobipic.model;

import com.mobipic.FxToolkit;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathAndTextTest {

    private static final ShapeStyle BRUSH = new ShapeStyle(Color.BLACK, Color.WHITE, 4, FillMode.OUTLINE);

    @BeforeAll
    static void startFx() {
        FxToolkit.start(); // text measurement needs fonts
    }

    @Test
    void pathGrowsAndHitTestsAlongItsSegments() {
        PathLayer path = new PathLayer(0, 0, BRUSH);
        for (int i = 1; i <= 100; i++) {
            path.addPoint(i, i % 2 == 0 ? 0 : 1);
        }
        assertEquals(101, path.pointCount());
        assertTrue(path.hitTest(50, 1));
        assertFalse(path.hitTest(50, 30));
        assertEquals(new Box(0, 0, 100, 1), path.bounds());
    }

    @Test
    void resizingAPathScalesItsPoints() {
        PathLayer path = new PathLayer(new double[]{0, 10, 20}, new double[]{0, 10, 0}, BRUSH);
        path.dragHandle(4, 40, 20, false); // bottom-right: double the size
        assertArrayEquals(new double[]{0, 20, 40}, path.getXs(), 1e-9);
        assertArrayEquals(new double[]{0, 20, 0}, path.getYs(), 1e-9);
    }

    @Test
    void flippingAPathMirrorsIt() {
        PathLayer path = new PathLayer(new double[]{0, 10}, new double[]{0, 10}, BRUSH);
        path.dragHandle(4, -10, 10, false); // bottom-right dragged past the left edge
        assertArrayEquals(new double[]{0, -10}, path.getXs(), 1e-9);
    }

    @Test
    void multiLineTextHasOneLineHeightPerLine() {
        TextLayer one = new TextLayer(10, 20, "Bonjour", TextStyle.DEFAULT);
        TextLayer three = new TextLayer(10, 20, "Bonjour\nle\nmonde", TextStyle.DEFAULT);
        assertEquals(3 * one.bounds().h(), three.bounds().h(), 1e-6);
        assertEquals(10, three.bounds().x());
        assertEquals(20, three.bounds().y());
        assertTrue(three.hitTest(15, 25));
    }

    @Test
    void resizingTextScalesTheFontAndKeepsTheAnchorCorner() {
        TextLayer text = new TextLayer(100, 100, "Mobipic", TextStyle.DEFAULT);
        Box before = text.bounds();
        // Drag the bottom-right corner (2) to make the text twice as tall
        text.dragHandle(2, before.maxX() + before.w(), before.maxY() + before.h(), false);
        assertEquals(2 * TextStyle.DEFAULT.size(), text.getStyle().size(), 0.5);
        assertEquals(100, text.getX(), 1e-6);
        assertEquals(100, text.getY(), 1e-6);
    }

    @Test
    void editingTextUpdatesItsBounds() {
        TextLayer text = new TextLayer(0, 0, "a", TextStyle.DEFAULT);
        double narrow = text.bounds().w();
        text.setText("a much longer line");
        assertTrue(text.bounds().w() > narrow);
    }

    @Test
    void textStyleChangesAreCopiedIndependently() {
        TextLayer text = new TextLayer(0, 0, "x", TextStyle.DEFAULT);
        TextLayer copy = text.copy();
        copy.setStyle(new TextStyle("Arial", 12, true, true, TextAlignment.CENTER, Color.RED, Color.BLACK, 2, true));
        copy.setText("y");
        assertEquals(TextStyle.DEFAULT, text.getStyle());
        assertEquals("x", text.getText());
    }
}
