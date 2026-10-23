package com.mobipic.tool;

import javafx.scene.input.KeyCode;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * The tools of the tool palette, with their single-key shortcut.
 */
public enum ToolKind {
    SELECT(KeyCode.V, SelectTool::new),
    BRUSH(KeyCode.B, BrushTool::new),
    RECTANGLE(KeyCode.R, () -> new ShapeTool(ShapeTool.Shape.RECTANGLE)),
    ROUNDED_RECTANGLE(KeyCode.U, () -> new ShapeTool(ShapeTool.Shape.ROUNDED_RECTANGLE)),
    ELLIPSE(KeyCode.E, () -> new ShapeTool(ShapeTool.Shape.ELLIPSE)),
    TRIANGLE(KeyCode.G, () -> new ShapeTool(ShapeTool.Shape.TRIANGLE)),
    LINE(KeyCode.L, () -> new ShapeTool(ShapeTool.Shape.LINE)),
    TEXT(KeyCode.T, TextTool::new),
    EYEDROPPER(KeyCode.I, EyedropperTool::new);

    private final KeyCode shortcut;
    private final Supplier<Tool> factory;

    ToolKind(KeyCode shortcut, Supplier<Tool> factory) {
        this.shortcut = shortcut;
        this.factory = factory;
    }

    public KeyCode shortcut() {
        return shortcut;
    }

    public Tool create() {
        return factory.get();
    }

    /** @return the i18n key of the tool name */
    public String key() {
        return "tool.name." + name().toLowerCase(Locale.ROOT);
    }

    /** @return the i18n key of the short help shown while the tool is active */
    public String hintKey() {
        return "tool.hint." + name().toLowerCase(Locale.ROOT);
    }

    public static ToolKind forShortcut(KeyCode code) {
        for (ToolKind kind : values()) {
            if (kind.shortcut == code) {
                return kind;
            }
        }
        return null;
    }
}
