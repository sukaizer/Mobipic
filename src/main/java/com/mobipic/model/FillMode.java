package com.mobipic.model;

import java.util.Locale;

/**
 * Which parts of a shape are painted.
 */
public enum FillMode {
    OUTLINE(true, false),
    FILL(false, true),
    OUTLINE_AND_FILL(true, true);

    private final boolean stroked;
    private final boolean filled;

    FillMode(boolean stroked, boolean filled) {
        this.stroked = stroked;
        this.filled = filled;
    }

    public boolean stroked() {
        return stroked;
    }

    public boolean filled() {
        return filled;
    }

    public String key() {
        return "fillMode." + name().toLowerCase(Locale.ROOT);
    }
}
