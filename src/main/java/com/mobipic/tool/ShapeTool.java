package com.mobipic.tool;

import com.mobipic.model.Box;
import com.mobipic.model.EllipseLayer;
import com.mobipic.model.Geometry;
import com.mobipic.model.LineLayer;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeLayer;
import com.mobipic.model.TriangleLayer;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;

import java.util.Locale;

/**
 * Draws shapes by press-drag-release, in any direction, as in Paint. The tool stays active so several
 * shapes can be drawn in a row; the last one stays selected so its options can still be changed.
 * <ul>
 *     <li>Shift: square / circle / equilateral-like triangle, lines snapped to 45°</li>
 *     <li>Alt: draw from the centre</li>
 *     <li>Triangle: a click without dragging places the vertices one by one (three clicks)</li>
 * </ul>
 */
public final class ShapeTool implements Tool {

    public enum Shape { RECTANGLE, ROUNDED_RECTANGLE, ELLIPSE, TRIANGLE, LINE }

    /** Below this size (in screen pixels) a drag is considered a plain click. */
    private static final double MIN_DRAG = 3;

    private final Shape shape;
    private ShapeLayer current;
    private double anchorX;
    private double anchorY;
    private boolean dragged;
    /** Number of triangle vertices placed by clicking, 0 when not in click mode. */
    private int clickedVertices;

    public ShapeTool(Shape shape) {
        this.shape = shape;
    }

    public Shape shape() {
        return shape;
    }

    @Override
    public void onPress(ToolContext ctx, ToolEvent e) {
        if (clickedVertices > 0) {
            placeNextVertex(ctx, e);
            return;
        }
        anchorX = e.x();
        anchorY = e.y();
        dragged = false;
        current = create(ctx, e.x(), e.y());
        ctx.model().add(current);
    }

    private ShapeLayer create(ToolContext ctx, double x, double y) {
        ToolSettings s = ctx.settings();
        ShapeLayer layer = switch (shape) {
            case RECTANGLE -> new RectangleLayer(x, y, 0, 0, s.shapeStyle());
            case ROUNDED_RECTANGLE -> new RectangleLayer(x, y, 0, 0, s.cornerRadiusProperty().get(), s.shapeStyle());
            case ELLIPSE -> new EllipseLayer(x, y, 0, 0, s.shapeStyle());
            case TRIANGLE -> new TriangleLayer(x, y, x, y, x, y, s.shapeStyle());
            case LINE -> new LineLayer(x, y, x, y, s.shapeStyle());
        };
        layer.setOpacity(s.opacityProperty().get());
        return layer;
    }

    @Override
    public void onDrag(ToolContext ctx, ToolEvent e) {
        if (clickedVertices > 0) {
            onMove(ctx, e);
            return;
        }
        if (current == null) {
            return;
        }
        if (Math.hypot(e.x() - anchorX, e.y() - anchorY) * ctx.zoom() >= MIN_DRAG) {
            dragged = true;
        }
        if (dragged) {
            reshape(e);
            ctx.model().changed();
        }
    }

    private void reshape(ToolEvent e) {
        if (current instanceof LineLayer line) {
            Point2D end = e.shift() ? Geometry.snapAngle(anchorX, anchorY, e.x(), e.y()) : new Point2D(e.x(), e.y());
            if (e.alt()) {
                line.setPoints(2 * anchorX - end.getX(), 2 * anchorY - end.getY(), end.getX(), end.getY());
            } else {
                line.setPoints(anchorX, anchorY, end.getX(), end.getY());
            }
            return;
        }
        double ratio = e.shift() ? 1 : 0;
        Box box = e.alt()
                ? Box.fromCenter(anchorX, anchorY, e.x(), e.y(), ratio)
                : Box.fromCorners(anchorX, anchorY, e.x(), e.y(), ratio);
        switch (current) {
            case RectangleLayer r -> r.setBox(box);
            case EllipseLayer el -> el.setBox(box);
            case TriangleLayer t -> t.setBox(box);
            default -> throw new IllegalStateException(current.getClass().getName());
        }
    }

    @Override
    public void onRelease(ToolContext ctx, ToolEvent e) {
        if (current == null || clickedVertices > 0) {
            return;
        }
        if (dragged) {
            reshape(e);
            ctx.model().changed();
            current = null;
        } else if (shape == Shape.TRIANGLE) {
            clickedVertices = 1; // plain click: place the other vertices by clicking
        } else {
            ctx.model().cancelAdd(current);
            current = null;
        }
    }

    @Override
    public void onMove(ToolContext ctx, ToolEvent e) {
        if (clickedVertices > 0 && current instanceof TriangleLayer triangle) {
            // Preview: the next vertices follow the cursor
            for (int i = clickedVertices; i < 3; i++) {
                triangle.setVertex(i, e.x(), e.y());
            }
            ctx.model().refresh();
        }
    }

    private void placeNextVertex(ToolContext ctx, ToolEvent e) {
        TriangleLayer triangle = (TriangleLayer) current;
        for (int i = clickedVertices; i < 3; i++) {
            triangle.setVertex(i, e.x(), e.y());
        }
        clickedVertices++;
        ctx.model().changed();
        if (clickedVertices == 3) {
            clickedVertices = 0;
            current = null;
        }
    }

    @Override
    public void cancel(ToolContext ctx) {
        if (clickedVertices > 0 && current != null) {
            ctx.model().cancelAdd(current);
        }
        clickedVertices = 0;
        current = null;
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        return Cursor.CROSSHAIR;
    }

    @Override
    public String hintKey() {
        return "tool.hint." + shape.name().toLowerCase(Locale.ROOT);
    }
}
