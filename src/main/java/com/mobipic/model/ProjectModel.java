package com.mobipic.model;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * An editing project: an ordered list of layers on top of a base image, the current selection,
 * the undo history and the saved state.
 * <p>
 * The first layer is always the base image. It defines the canvas size and cannot be moved,
 * resized, reordered or deleted.
 */
public final class ProjectModel {

    /** Offset applied to duplicated layers so the copy is visible. */
    static final double DUPLICATE_OFFSET = 10;
    private static final int HISTORY_LIMIT = 50;

    private final ObservableList<Layer> layers = FXCollections.observableArrayList();
    private final ObservableList<Layer> readOnlyLayers = FXCollections.unmodifiableObservableList(layers);
    private final ReadOnlyObjectWrapper<Layer> selected = new ReadOnlyObjectWrapper<>(this, "selected");
    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper(this, "dirty");
    private final ReadOnlyObjectWrapper<Path> file = new ReadOnlyObjectWrapper<>(this, "file");
    private final ReadOnlyLongWrapper revision = new ReadOnlyLongWrapper(this, "revision");
    private final History history = new History(HISTORY_LIMIT);
    private final double width;
    private final double height;

    /** Creates a new project from a base image. */
    public ProjectModel(Image baseImage) {
        this(baseImage.getWidth(), baseImage.getHeight(), List.of(new ImageLayer(baseImage, 0, 0)));
    }

    /** Restores a project; {@code layers} must start with the base image layer. */
    public ProjectModel(double width, double height, List<? extends Layer> layers) {
        if (layers.isEmpty() || !(layers.getFirst() instanceof ImageLayer)) {
            throw new IllegalArgumentException("A project must start with a base image layer");
        }
        this.width = width;
        this.height = height;
        this.layers.setAll(layers);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    /** @return the layers, bottom first (read-only: use the model operations to change it) */
    public ObservableList<Layer> getLayers() {
        return readOnlyLayers;
    }

    public ImageLayer getBase() {
        return (ImageLayer) layers.getFirst();
    }

    public boolean isBase(Layer layer) {
        return layer != null && layers.getFirst() == layer;
    }

    /** @return whether the layer belongs to the project and may be moved, deleted or duplicated */
    public boolean isEditable(Layer layer) {
        return layer != null && !isBase(layer) && layers.contains(layer);
    }

    // ---- Selection --------------------------------------------------------------------------

    public ReadOnlyObjectProperty<Layer> selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    public Layer getSelected() {
        return selected.get();
    }

    /** Selects a layer of the project, or nothing when {@code null}. */
    public void select(Layer layer) {
        if (layer != null && !layers.contains(layer)) {
            throw new IllegalArgumentException("The layer is not part of the project");
        }
        selected.set(layer);
    }

    /** @return the top-most visible layer under the point, or {@code null} */
    public Layer layerAt(double x, double y) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).hitTest(x, y)) {
                return layers.get(i);
            }
        }
        return null;
    }

    // ---- Edition ----------------------------------------------------------------------------

    /** Adds a layer on top and selects it. */
    public void add(Layer layer) {
        checkpoint();
        layers.add(layer);
        selected.set(layer);
        changed();
    }

    /** Removes a layer that was just added, without leaving a trace in the history. */
    public void cancelAdd(Layer layer) {
        if (layers.getLast() == layer && !isBase(layer)) {
            if (selected.get() == layer) {
                selected.set(null);
            }
            layers.removeLast();
            history.dropLatest();
            changed();
        }
    }

    public void remove(Layer layer) {
        if (!isEditable(layer)) {
            return;
        }
        checkpoint();
        if (selected.get() == layer) {
            selected.set(null);
        }
        layers.remove(layer);
        changed();
    }

    /** Inserts a copy of the layer just above it, slightly offset, and selects it. */
    public Layer duplicate(Layer layer) {
        if (!isEditable(layer)) {
            return null;
        }
        checkpoint();
        Layer copy = layer.copy();
        copy.translate(DUPLICATE_OFFSET, DUPLICATE_OFFSET);
        layers.add(layers.indexOf(layer) + 1, copy);
        selected.set(copy);
        changed();
        return copy;
    }

    /** Shows or hides a layer. Hidden layers are not painted, exported or clickable. */
    public void setVisible(Layer layer, boolean visible) {
        if (layer != null && layers.contains(layer) && layer.isVisible() != visible) {
            checkpoint();
            layer.setVisible(visible);
            changed();
        }
    }

    /** Renames a layer; a blank name restores the default type name. */
    public void rename(Layer layer, String name) {
        if (layer != null && layers.contains(layer) && !java.util.Objects.equals(layer.getName(), normalize(name))) {
            checkpoint();
            layer.setName(name);
            changed();
        }
    }

    private static String normalize(String name) {
        return name == null || name.isBlank() ? null : name.strip();
    }

    /** Rotates an editable layer around its centre. */
    public void rotate(Layer layer, double degrees) {
        if (isEditable(layer)) {
            checkpoint();
            javafx.geometry.Point2D c = layer.center();
            layer.rotateBy(degrees, c.getX(), c.getY());
            changed();
        }
    }

    /**
     * Moves an editable layer to another position in the list (drag and drop in the layer panel).
     * The base image always stays first.
     */
    public void moveTo(Layer layer, int index) {
        if (!isEditable(layer)) {
            return;
        }
        int target = Math.clamp(index, 1, layers.size() - 1);
        int current = layers.indexOf(layer);
        if (target == current) {
            return;
        }
        checkpoint();
        Layer previousSelection = selected.get();
        layers.remove(current);
        layers.add(target, layer);
        selected.set(previousSelection);
        changed();
    }

    /** @return whether the layer can go one step further back (never below the base image) */
    public boolean canSendBackward(Layer layer) {
        return isEditable(layer) && layers.indexOf(layer) > 1;
    }

    public boolean canBringForward(Layer layer) {
        return isEditable(layer) && layers.indexOf(layer) < layers.size() - 1;
    }

    public void sendBackward(Layer layer) {
        if (canSendBackward(layer)) {
            swapWith(layer, -1);
        }
    }

    public void bringForward(Layer layer) {
        if (canBringForward(layer)) {
            swapWith(layer, +1);
        }
    }

    private void swapWith(Layer layer, int offset) {
        checkpoint();
        int index = layers.indexOf(layer);
        Collections.swap(layers, index, index + offset);
        changed();
    }

    /**
     * Records the current state in the undo history. Call it before changing a layer in place
     * (move, resize, filter), then call {@link #changed()}.
     */
    public void checkpoint() {
        history.push(snapshot());
    }

    /** Forgets the last checkpoint, when the change it was taken for did not happen. */
    public void discardCheckpoint() {
        history.dropLatest();
    }

    /** Notifies that a layer was changed in place: marks the project as modified and triggers a repaint. */
    public void changed() {
        dirty.set(true);
        revision.set(revision.get() + 1);
    }

    /** Triggers a repaint without marking the project as modified (e.g. a preview). */
    public void refresh() {
        revision.set(revision.get() + 1);
    }

    // ---- Undo / redo ------------------------------------------------------------------------

    public void undo() {
        if (history.canUndoProperty().get()) {
            restore(history.undo(snapshot()));
        }
    }

    public void redo() {
        if (history.canRedoProperty().get()) {
            restore(history.redo(snapshot()));
        }
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        return history.canUndoProperty();
    }

    public ReadOnlyBooleanProperty canRedoProperty() {
        return history.canRedoProperty();
    }

    private List<Layer> snapshot() {
        return layers.stream().map(Layer::copy).toList();
    }

    private void restore(List<Layer> snapshot) {
        int selectedIndex = layers.indexOf(selected.get());
        boolean sameCount = snapshot.size() == layers.size();
        selected.set(null);
        layers.setAll(snapshot);
        // Keep the selection on the same slot when the layer count did not change (move, resize, filter...)
        if (selectedIndex >= 0 && sameCount) {
            selected.set(layers.get(selectedIndex));
        }
        changed();
    }

    // ---- Saved state ------------------------------------------------------------------------

    /** Incremented on every change, including in-place changes that do not alter the layer list. */
    public ReadOnlyLongProperty revisionProperty() {
        return revision.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty dirtyProperty() {
        return dirty.getReadOnlyProperty();
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public ReadOnlyObjectProperty<Path> fileProperty() {
        return file.getReadOnlyProperty();
    }

    public Path getFile() {
        return file.get();
    }

    /** Marks the project as saved to (or opened from) the given file. */
    public void markSaved(Path path) {
        file.set(path);
        dirty.set(false);
    }
}
