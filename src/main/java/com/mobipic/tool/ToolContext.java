package com.mobipic.tool;

import com.mobipic.model.ProjectModel;

/**
 * What a tool can see and do while handling canvas events.
 */
public interface ToolContext {

    ProjectModel model();

    /** @return the current zoom factor (1 = 100%) */
    double zoom();

    /** Requests a repaint for overlay-only changes (model changes repaint automatically). */
    void repaint();

    /** Called by one-shot tools once their job is done, to go back to the default tool. */
    void toolFinished();
}
