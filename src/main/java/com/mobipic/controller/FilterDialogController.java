package com.mobipic.controller;

import com.mobipic.model.ColorFilter;
import com.mobipic.model.ImageLayer;
import com.mobipic.model.ProjectModel;
import com.mobipic.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.util.StringConverter;

/**
 * Applies a colour filter to an image layer, previewed live. Cancelling restores the previous filter.
 */
public final class FilterDialogController {

    @FXML
    private ChoiceBox<ColorFilter> choiceBox;

    private ProjectModel model;
    private ImageLayer layer;
    private ColorFilter original;
    private boolean committed;

    @FXML
    private void initialize() {
        choiceBox.getItems().setAll(ColorFilter.values());
        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ColorFilter filter) {
                return filter == null ? "" : I18n.get(filter.key());
            }

            @Override
            public ColorFilter fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
    }

    public void init(ProjectModel model, ImageLayer layer) {
        this.model = model;
        this.layer = layer;
        this.original = layer.getFilter();
        choiceBox.setValue(original);
        choiceBox.valueProperty().addListener((obs, old, filter) -> preview(filter));
        // Closing the window any other way than "OK" cancels the change
        choiceBox.getScene().getWindow().setOnHidden(e -> {
            if (!committed) {
                preview(original);
            }
        });
    }

    private void preview(ColorFilter filter) {
        layer.setFilter(filter);
        model.refresh();
    }

    @FXML
    private void validate() {
        ColorFilter chosen = choiceBox.getValue();
        if (chosen != original) {
            layer.setFilter(original);
            model.checkpoint(); // one undo step for the whole dialog
            layer.setFilter(chosen);
            model.changed();
        }
        committed = true;
        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        choiceBox.getScene().getWindow().hide();
    }
}
