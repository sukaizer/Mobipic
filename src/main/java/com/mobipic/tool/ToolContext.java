package com.mobipic.tool;

import com.mobipic.model.ProjectModel;
import com.mobipic.model.TextLayer;
import javafx.scene.paint.Color;

/**
 * What a tool can see and do while handling canvas events.
 */
public interface ToolContext {

    ProjectModel model();

    ToolSettings settings();

    /** @return the current zoom factor (1 = 100%) */
    double zoom();

    /** Requests a repaint for overlay-only changes (model changes repaint automatically). */
    void repaint();

    /** Starts typing into a text layer directly on the canvas. */
    void editText(TextLayer layer, boolean isNew);

    /** @return the colour of the flattened image at the canvas point, or {@code null} outside the image */
    Color colorAt(double x, double y);
}
