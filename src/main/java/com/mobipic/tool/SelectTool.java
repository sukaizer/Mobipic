package com.mobipic.tool;

/**
 * Default tool: clicking selects the top-most layer under the cursor.
 */
public final class SelectTool implements Tool {

    @Override
    public void onPress(ToolContext ctx, double x, double y) {
        ctx.model().select(ctx.model().layerAt(x, y));
    }

    @Override
    public String hintKey() {
        return "tool.select";
    }
}
