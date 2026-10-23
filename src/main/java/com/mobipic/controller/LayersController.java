package com.mobipic.controller;

import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.util.Fxml;
import com.mobipic.util.I18n;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * The layer list, kept in sync with the project selection in both directions.
 * Its context menu items are wired by {@link MainController}.
 */
public final class LayersController {

    private static final double ICON_SIZE = 16;

    @FXML
    ListView<Layer> layersList;
    @FXML
    MenuItem delete;
    @FXML
    MenuItem move;
    @FXML
    MenuItem resize;
    @FXML
    MenuItem duplicate;
    @FXML
    MenuItem filters;

    private final Map<String, Image> icons = new HashMap<>();
    private final InvalidationListener syncListener = obs -> syncSelectionFromModel();
    private ProjectModel model;
    private boolean syncing;

    @FXML
    private void initialize() {
        layersList.setCellFactory(list -> new LayerCell());
        layersList.getSelectionModel().selectedItemProperty().addListener((obs, old, layer) -> {
            if (!syncing && model != null) {
                model.select(layer);
            }
        });
    }

    public void setModel(ProjectModel newModel) {
        if (model != null) {
            model.selectedProperty().removeListener(syncListener);
            model.getLayers().removeListener(syncListener);
        }
        model = newModel;
        if (model == null) {
            layersList.setItems(FXCollections.emptyObservableList());
            return;
        }
        layersList.setItems(model.getLayers());
        model.selectedProperty().addListener(syncListener);
        // Reordering replaces list items, which drops the ListView selection: restore it
        model.getLayers().addListener(syncListener);
        syncSelectionFromModel();
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

    private Image iconFor(Layer layer) {
        // "layer.square" -> square.png
        String name = layer.typeKey().substring(layer.typeKey().indexOf('.') + 1);
        return icons.computeIfAbsent(name, n -> Fxml.image(n + ".png"));
    }

    private final class LayerCell extends ListCell<Layer> {
        private final ImageView icon = new ImageView();

        LayerCell() {
            icon.setFitWidth(ICON_SIZE);
            icon.setFitHeight(ICON_SIZE);
            icon.setPreserveRatio(true);
            icon.setSmooth(true);
        }

        @Override
        protected void updateItem(Layer layer, boolean empty) {
            super.updateItem(layer, empty);
            if (empty || layer == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(model != null && model.isBase(layer) ? I18n.get("layer.base") : I18n.get(layer.typeKey()));
            icon.setImage(iconFor(layer));
            setGraphic(icon);
        }
    }
}
