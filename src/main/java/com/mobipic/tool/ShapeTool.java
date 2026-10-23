package com.mobipic.tool;

import com.mobipic.model.ShapeLayer;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.ShapeType;
import com.mobipic.model.TriangleLayer;
import javafx.scene.Cursor;

/**
 * Draws one shape, then hands control back to the default tool.
 * <p>
 * Most shapes are drawn by press-drag-release from one corner (or end) to the other, in any direction.
 * Triangles are drawn with three clicks, one per vertex, with a live preview between clicks.
 */
public final class ShapeTool implements Tool {

    /** Below this size (in canvas pixels) a drag is considered a plain click and discarded. */
    private static final double MIN_DRAG = 2;

    private final ShapeType type;
    private final ShapeStyle style;
    private ShapeLayer current;
    private double anchorX;
    private double anchorY;
    private int trianglePoints;

    public ShapeTool(ShapeType type, ShapeStyle style) {
        this.type = type;
        this.style = style;
    }

    @Override
    public void onPress(ToolContext ctx, double x, double y) {
        if (type == ShapeType.TRIANGLE) {
            triangleClick(ctx, x, y);
            return;
        }
        anchorX = x;
        anchorY = y;
        current = type.create(x, y, style);
        ctx.model().add(current);
    }

    @Override
    public void onDrag(ToolContext ctx, double x, double y) {
        if (type == ShapeType.TRIANGLE) {
            onMove(ctx, x, y);
        } else if (current != null) {
            current.setFromDrag(anchorX, anchorY, x, y);
            ctx.model().changed();
        }
    }

    @Override
    public void onRelease(ToolContext ctx, double x, double y) {
        if (type == ShapeType.TRIANGLE || current == null) {
            return;
        }
        current.setFromDrag(anchorX, anchorY, x, y);
        if (Math.abs(x - anchorX) < MIN_DRAG && Math.abs(y - anchorY) < MIN_DRAG) {
            ctx.model().cancelAdd(current);
            current = null;
            return; // stay in drawing mode, a real drag is expected
        }
        ctx.model().changed();
        current = null;
        ctx.toolFinished();
    }

    @Override
    public void onMove(ToolContext ctx, double x, double y) {
        if (current instanceof TriangleLayer triangle) {
            // Preview: the next vertex (and the following ones) follow the cursor
            for (int i = trianglePoints; i < 3; i++) {
                triangle.moveHandle(i, x, y);
            }
            ctx.model().changed();
        }
    }

    private void triangleClick(ToolContext ctx, double x, double y) {
        if (current == null) {
            current = type.create(x, y, style);
            trianglePoints = 1;
            ctx.model().add(current);
            return;
        }
        for (int i = trianglePoints; i < 3; i++) {
            current.moveHandle(i, x, y);
        }
        trianglePoints++;
        ctx.model().changed();
        if (trianglePoints == 3) {
            current = null;
            ctx.toolFinished();
        }
    }

    @Override
    public void cancel(ToolContext ctx) {
        if (current != null) {
            ctx.model().cancelAdd(current);
            current = null;
        }
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        return Cursor.CROSSHAIR;
    }

    @Override
    public String hintKey() {
        return type.hintKey();
    }
}
