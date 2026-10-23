package com.mobipic.tool;

import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import javafx.scene.Cursor;

/**
 * Drags layers around. Pressing on another layer selects it first.
 */
public final class MoveTool implements Tool {

    private Layer dragged;
    private boolean moved;
    private double lastX;
    private double lastY;

    @Override
    public void onPress(ToolContext ctx, double x, double y) {
        ProjectModel model = ctx.model();
        Layer selected = model.getSelected();
        Layer target = selected != null && model.isEditable(selected) && selected.contains(x, y)
                ? selected
                : model.layerAt(x, y);
        model.select(target);
        dragged = model.isEditable(target) ? target : null;
        moved = false;
        lastX = x;
        lastY = y;
    }

    @Override
    public void onDrag(ToolContext ctx, double x, double y) {
        if (dragged == null) {
            return;
        }
        if (!moved) {
            ctx.model().checkpoint();
            moved = true;
        }
        dragged.translate(x - lastX, y - lastY);
        lastX = x;
        lastY = y;
        ctx.model().changed();
    }

    @Override
    public void onRelease(ToolContext ctx, double x, double y) {
        dragged = null;
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        if (dragged != null) {
            return Cursor.CLOSED_HAND;
        }
        return ctx.model().isEditable(ctx.model().layerAt(x, y)) ? Cursor.OPEN_HAND : Cursor.DEFAULT;
    }

    @Override
    public String hintKey() {
        return "tool.move";
    }
}
