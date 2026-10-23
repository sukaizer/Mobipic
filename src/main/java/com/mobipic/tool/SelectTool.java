package com.mobipic.tool;

import com.mobipic.model.Geometry;
import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.TextLayer;
import com.mobipic.render.CanvasRenderer;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * Direct manipulation, as in most editors: click to select, drag a layer to move it, drag a handle
 * to resize it (Shift keeps proportions), drag the round handle to rotate it (Shift snaps to 15°).
 * Double-clicking a text edits it.
 */
public final class SelectTool implements Tool {

    private enum Mode { NONE, MOVE, RESIZE, ROTATE }

    private static final Cursor[] RESIZE_CURSORS = {
            Cursor.NW_RESIZE, Cursor.N_RESIZE, Cursor.NE_RESIZE, Cursor.E_RESIZE,
            Cursor.SE_RESIZE, Cursor.S_RESIZE, Cursor.SW_RESIZE, Cursor.W_RESIZE};

    private Mode mode = Mode.NONE;
    private Layer target;
    private boolean changed;
    private int handle;
    private double lastX;
    private double lastY;
    private Point2D pivot;
    private double startAngle;
    private double appliedRotation;

    @Override
    public void onPress(ToolContext ctx, ToolEvent e) {
        ProjectModel model = ctx.model();
        Layer selected = model.getSelected();
        mode = Mode.NONE;
        changed = false;

        if (model.isEditable(selected)) {
            if (onRotationHandle(ctx, selected, e.x(), e.y())) {
                start(Mode.ROTATE, selected, e);
                pivot = selected.center();
                startAngle = Geometry.angle(pivot.getX(), pivot.getY(), e.x(), e.y());
                appliedRotation = 0;
                return;
            }
            int h = handleAt(ctx, selected, e.x(), e.y());
            if (h >= 0) {
                start(Mode.RESIZE, selected, e);
                handle = h;
                return;
            }
        }

        // Keep the current selection when clicking inside it, even if another layer is on top
        Layer hit = selected != null && selected.hitTest(e.x(), e.y()) ? selected : model.layerAt(e.x(), e.y());
        model.select(hit);
        if (e.clickCount() == 2 && hit instanceof TextLayer text && model.isEditable(text)) {
            ctx.editText(text, false);
            return;
        }
        if (model.isEditable(hit)) {
            start(Mode.MOVE, hit, e);
        }
    }

    private void start(Mode newMode, Layer layer, ToolEvent e) {
        mode = newMode;
        target = layer;
        lastX = e.x();
        lastY = e.y();
    }

    @Override
    public void onDrag(ToolContext ctx, ToolEvent e) {
        if (mode == Mode.NONE) {
            return;
        }
        if (!changed) {
            ctx.model().checkpoint();
            changed = true;
        }
        switch (mode) {
            case MOVE -> target.translate(e.x() - lastX, e.y() - lastY);
            case RESIZE -> handle = target.dragHandle(handle, e.x(), e.y(), e.shift());
            case ROTATE -> {
                double total = Geometry.angle(pivot.getX(), pivot.getY(), e.x(), e.y()) - startAngle;
                if (e.shift()) {
                    total = Math.round(total / Geometry.SNAP_DEGREES) * Geometry.SNAP_DEGREES;
                }
                target.rotateBy(total - appliedRotation, pivot.getX(), pivot.getY());
                appliedRotation = total;
            }
            default -> throw new IllegalStateException();
        }
        lastX = e.x();
        lastY = e.y();
        ctx.model().changed();
    }

    @Override
    public void onRelease(ToolContext ctx, ToolEvent e) {
        mode = Mode.NONE;
        target = null;
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        switch (mode) {
            case MOVE:
                return Cursor.MOVE;
            case ROTATE:
                return Cursor.HAND;
            case RESIZE:
                return resizeCursor(target, handle);
            default:
                break;
        }
        ProjectModel model = ctx.model();
        Layer selected = model.getSelected();
        if (model.isEditable(selected)) {
            if (onRotationHandle(ctx, selected, x, y)) {
                return Cursor.HAND;
            }
            int h = handleAt(ctx, selected, x, y);
            if (h >= 0) {
                return resizeCursor(selected, h);
            }
        }
        return model.isEditable(model.layerAt(x, y)) ? Cursor.MOVE : Cursor.DEFAULT;
    }

    /** Picks the resize cursor matching the on-screen direction of the handle (taking the rotation into account). */
    private static Cursor resizeCursor(Layer layer, int index) {
        List<Point2D> handles = layer.handles();
        if (handles.size() != 8) {
            return Cursor.CROSSHAIR; // vertices of lines and triangles, text corners
        }
        int steps = (int) Math.round(layer.getRotation() / 45);
        return RESIZE_CURSORS[Math.floorMod(index + steps, 8)];
    }

    @Override
    public void paintOverlay(ToolContext ctx, GraphicsContext gc) {
        Layer selected = ctx.model().getSelected();
        if (ctx.model().isEditable(selected)) {
            CanvasRenderer.paintHandles(gc, selected, ctx.zoom());
        }
    }

    static int handleAt(ToolContext ctx, Layer layer, double x, double y) {
        double tolerance = CanvasRenderer.HANDLE_SIZE / ctx.zoom();
        List<Point2D> handles = layer.handles();
        for (int i = 0; i < handles.size(); i++) {
            Point2D h = layer.toWorld(handles.get(i).getX(), handles.get(i).getY());
            if (h.distance(x, y) <= tolerance) {
                return i;
            }
        }
        return -1;
    }

    private static boolean onRotationHandle(ToolContext ctx, Layer layer, double x, double y) {
        return CanvasRenderer.rotationHandle(layer, ctx.zoom()).distance(x, y) <= CanvasRenderer.HANDLE_SIZE / ctx.zoom();
    }

    @Override
    public String hintKey() {
        return ToolKind.SELECT.hintKey();
    }
}
