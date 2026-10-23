package com.mobipic.model;

import javafx.scene.paint.Color;

/**
 * A translucent colour overlay applied on top of an image layer.
 */
public enum ColorFilter {
    NONE(Color.TRANSPARENT),
    BLUE(Color.web("#0066ff", 0.4)),
    GREEN(Color.web("#00ff00", 0.4)),
    PINK(Color.web("#ff66ff", 0.4)),
    RED(Color.web("#ff0000", 0.4));

    private final Color overlay;

    ColorFilter(Color overlay) {
        this.overlay = overlay;
    }

    public Color overlay() {
        return overlay;
    }

    public String key() {
        return "filter." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
