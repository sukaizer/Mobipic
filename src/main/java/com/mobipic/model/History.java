package com.mobipic.model;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Bounded undo/redo stacks of layer list snapshots.
 */
final class History {

    private final Deque<List<Layer>> undo = new ArrayDeque<>();
    private final Deque<List<Layer>> redo = new ArrayDeque<>();
    private final int limit;
    private final ReadOnlyBooleanWrapper canUndo = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canRedo = new ReadOnlyBooleanWrapper();

    History(int limit) {
        this.limit = limit;
    }

    /** Records the state preceding a change; clears the redo stack. */
    void push(List<Layer> snapshot) {
        undo.push(snapshot);
        if (undo.size() > limit) {
            undo.removeLast();
        }
        redo.clear();
        update();
    }

    /** Forgets the latest recorded state, used when a change is cancelled. */
    void dropLatest() {
        undo.poll();
        update();
    }

    List<Layer> undo(List<Layer> current) {
        redo.push(current);
        List<Layer> previous = undo.pop();
        update();
        return previous;
    }

    List<Layer> redo(List<Layer> current) {
        undo.push(current);
        List<Layer> next = redo.pop();
        update();
        return next;
    }

    ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo.getReadOnlyProperty();
    }

    ReadOnlyBooleanProperty canRedoProperty() {
        return canRedo.getReadOnlyProperty();
    }

    private void update() {
        canUndo.set(!undo.isEmpty());
        canRedo.set(!redo.isEmpty());
    }
}
