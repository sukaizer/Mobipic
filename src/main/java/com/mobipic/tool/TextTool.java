package com.mobipic.tool;

import com.mobipic.model.Layer;
import com.mobipic.model.TextLayer;
import javafx.scene.Cursor;

/**
 * Click on the canvas and type, as in Paint. Clicking an existing text edits it.
 */
public final class TextTool implements Tool {

    @Override
    public void onPress(ToolContext ctx, ToolEvent e) {
        Layer hit = ctx.model().layerAt(e.x(), e.y());
        if (hit instanceof TextLayer text && ctx.model().isEditable(text)) {
            ctx.model().select(text);
            ctx.editText(text, false);
            return;
        }
        ToolSettings s = ctx.settings();
        TextLayer text = new TextLayer(e.x(), e.y(), "", s.textStyle());
        text.setOpacity(s.opacityProperty().get());
        ctx.model().add(text);
        ctx.editText(text, true);
    }

    @Override
    public Cursor cursorAt(ToolContext ctx, double x, double y) {
        return Cursor.TEXT;
    }

    @Override
    public String hintKey() {
        return "tool.hint.text";
    }
}
