package com.mobipic.controller;

import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.render.CanvasRenderer;
import com.mobipic.util.I18n;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The layer list, kept in sync with the project selection in both directions. Each row shows a
 * visibility toggle, a thumbnail and the layer name. Rows can be renamed (double-click or F2) and
 * reordered by drag and drop. Its context menu items are wired by {@link MainController}.
 */
public final class LayersController {

    private static final double THUMB_WIDTH = 40;
    private static final double THUMB_HEIGHT = 30;
    private static final String EYE_OPEN = "M1 8 Q8 1 15 8 Q8 15 1 8 Z M8 5.5 A2.5 2.5 0 1 0 8 10.5 A2.5 2.5 0 1 0 8 5.5 Z";
    private static final String EYE_CLOSED = "M1 8 Q8 15 15 8 M3 10.5 L1.5 12.5 M8 12 L8 14.5 M13 10.5 L14.5 12.5";

    @FXML
    ListView<Layer> layersList;
    @FXML
    MenuItem rename;
    @FXML
    MenuItem toggleVisibility;
    @FXML
    MenuItem duplicate;
    @FXML
    MenuItem delete;
    @FXML
    MenuItem rotateLeft;
    @FXML
    MenuItem rotateRight;
    @FXML
    MenuItem filters;

    private final Map<Layer, Image> thumbnails = new IdentityHashMap<>();
    private final InvalidationListener syncListener = obs -> syncSelectionFromModel();
    /** Thumbnails are refreshed shortly after the last change, not on every mouse move. */
    private final PauseTransition thumbnailRefresh = new PauseTransition(Duration.millis(250));
    private final InvalidationListener revisionListener = obs -> thumbnailRefresh.playFromStart();
    private ProjectModel model;
    private boolean syncing;
    private Layer dragged;

    @FXML
    private void initialize() {
        layersList.setCellFactory(list -> new LayerCell());
        // Renaming goes through the model (LayerCell.commitEdit): the default handler would replace the list item
        layersList.setOnEditCommit(e -> { });
        layersList.getSelectionModel().selectedItemProperty().addListener((obs, old, layer) -> {
            if (!syncing && model != null) {
                model.select(layer);
            }
        });
        layersList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F2) {
                startRename();
                e.consume();
            }
        });
        thumbnailRefresh.setOnFinished(e -> {
            thumbnails.clear();
            layersList.refresh();
        });
    }

    public void setModel(ProjectModel newModel) {
        if (model != null) {
            model.selectedProperty().removeListener(syncListener);
            model.getLayers().removeListener(syncListener);
            model.revisionProperty().removeListener(revisionListener);
        }
        model = newModel;
        thumbnails.clear();
        if (model == null) {
            layersList.setItems(FXCollections.emptyObservableList());
            return;
        }
        layersList.setItems(model.getLayers());
        model.selectedProperty().addListener(syncListener);
        // Reordering replaces list items, which drops the ListView selection: restore it
        model.getLayers().addListener(syncListener);
        model.revisionProperty().addListener(revisionListener);
        syncSelectionFromModel();
    }

    /** Starts editing the name of the selected layer. */
    public void startRename() {
        int index = layersList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            layersList.edit(index);
        }
    }

    private void syncSelectionFromModel() {
        syncing = true;
        try {
            Layer selected = model.getSelected();
            if (selected == null) {
                layersList.getSelectionModel().clearSelection();
            } else {
                layersList.getSelectionModel().select(selected);
                layersList.scrollTo(selected);
            }
        } finally {
            syncing = false;
        }
    }

    private String displayName(Layer layer) {
        if (layer.getName() != null) {
            return layer.getName();
        }
        return model != null && model.isBase(layer) ? I18n.get("layer.base") : I18n.get(layer.typeKey());
    }

    /** Renders the layer alone, placed where it is in the image, scaled down to the thumbnail size. */
    private Image thumbnail(Layer layer) {
        return thumbnails.computeIfAbsent(layer, l -> {
            double scale = Math.min(THUMB_WIDTH / model.getWidth(), THUMB_HEIGHT / model.getHeight());
            int w = Math.max(1, (int) Math.round(model.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(model.getHeight() * scale));
            Canvas canvas = new Canvas(w, h);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.rgb(235, 235, 235));
            gc.fillRect(0, 0, w, h);
            gc.scale(scale, scale);
            CanvasRenderer.paintLayer(gc, l);
            return canvas.snapshot(new SnapshotParameters(), new WritableImage(w, h));
        });
    }

    private final class LayerCell extends ListCell<Layer> {
        private final ToggleButton eye = new ToggleButton();
        private final SVGPath eyeIcon = new SVGPath();
        private final ImageView thumb = new ImageView();
        private final Label name = new Label();
        private final HBox row = new HBox(6, eye, thumb, name);
        private TextField editor;

        LayerCell() {
            eye.getStyleClass().add("eye-toggle");
            eyeIcon.getStyleClass().add("eye-icon");
            eye.setGraphic(eyeIcon);
            eye.setFocusTraversable(false);
            eye.setTooltip(new Tooltip(I18n.get("action.toggleVisibility")));
            eye.setOnAction(e -> {
                if (getItem() != null && model != null) {
                    model.setVisible(getItem(), !getItem().isVisible());
                }
            });
            thumb.getStyleClass().add("layer-thumb");
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);
            row.setAlignment(Pos.CENTER_LEFT);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && getItem() != null) {
                    startEdit();
                }
            });
            installDragAndDrop();
        }

        private void installDragAndDrop() {
            setOnDragDetected(e -> {
                Layer layer = getItem();
                if (layer == null || model == null || !model.isEditable(layer) || isEditing()) {
                    return;
                }
                dragged = layer;
                Dragboard board = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(displayName(layer));
                board.setContent(content);
                board.setDragView(thumbnail(layer));
                e.consume();
            });
            setOnDragOver(e -> {
                if (dragged != null && e.getGestureSource() != this) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });
            setOnDragEntered(e -> {
                if (dragged != null && e.getGestureSource() != this) {
                    getStyleClass().add("drop-target");
                }
            });
            setOnDragExited(e -> getStyleClass().remove("drop-target"));
            setOnDragDropped(e -> {
                boolean done = dragged != null && model != null;
                if (done) {
                    int target = isEmpty() ? model.getLayers().size() - 1 : getIndex();
                    model.moveTo(dragged, target);
                }
                e.setDropCompleted(done);
                e.consume();
            });
            setOnDragDone(e -> dragged = null);
        }

        @Override
        protected void updateItem(Layer layer, boolean empty) {
            super.updateItem(layer, empty);
            if (empty || layer == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            eye.setSelected(layer.isVisible());
            eyeIcon.setContent(layer.isVisible() ? EYE_OPEN : EYE_CLOSED);
            row.setOpacity(layer.isVisible() ? 1 : 0.5);
            thumb.setImage(thumbnail(layer));
            name.setText(displayName(layer));
            if (isEditing()) {
                showEditor();
            } else {
                setGraphic(row);
            }
        }

        @Override
        public void startEdit() {
            if (getItem() == null) {
                return;
            }
            super.startEdit();
            if (isEditing()) {
                showEditor();
            }
        }

        private void showEditor() {
            if (editor == null) {
                editor = new TextField();
                editor.setOnAction(e -> commitEdit(getItem()));
                editor.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                        e.consume();
                    }
                });
                editor.focusedProperty().addListener((obs, was, focused) -> {
                    if (!focused && isEditing()) {
                        commitEdit(getItem());
                    }
                });
            }
            editor.setText(displayName(getItem()));
            HBox editRow = new HBox(6, thumb, editor);
            editRow.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(editor, Priority.ALWAYS);
            setGraphic(editRow);
            editor.requestFocus();
            editor.selectAll();
        }

        @Override
        public void commitEdit(Layer layer) {
            String typed = editor == null ? null : editor.getText();
            super.commitEdit(layer);
            if (model != null && layer != null && typed != null) {
                // Typing the default name back (or nothing) clears the custom name
                String defaultName = model.isBase(layer) ? I18n.get("layer.base") : I18n.get(layer.typeKey());
                model.rename(layer, typed.strip().equals(defaultName) ? null : typed);
            }
            restoreRow();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            restoreRow();
        }

        private void restoreRow() {
            row.getChildren().setAll(eye, thumb, name);
            setGraphic(getItem() == null ? null : row);
        }
    }
}
