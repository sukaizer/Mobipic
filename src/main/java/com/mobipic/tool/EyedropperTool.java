package com.mobipic.tool;

import javafx.scene.Cursor;
import javafx.scene.paint.Color;

/**
 * Picks a colour from the image: left click sets the primary colour, right click the secondary one.
 */
public final class EyedropperTool implements Tool {

    @Override
    public void onPress(ToolContext ctx, ToolEvent e) {
        pick(ctx, e);
    }

    @Override
    public void onDrag(ToolContext ctx, ToolEvent e) {
        pick(ctx, e);
    }

    private void pick(ToolContext ctx, ToolEvent e) {
        Color color = ctx.colorAt(e.x(), e.y());
        if (color != null) {
            (e.secondary() ? ctx.settings().secondaryProperty() : ctx.settings().primaryProperty()).set(color);
        }
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        return Cursor.CROSSHAIR;
    }

    @Override
    public String hintKey() {
        return "tool.hint.eyedropper";
    }
}
