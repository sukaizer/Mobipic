package com.mobipic.controller;

import com.mobipic.model.TextLayer;
import com.mobipic.util.I18n;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * Text editor window: content, font family, size, colour, bold and italic, with a live preview.
 */
public final class TextDialogController {

    private static final String DEFAULT_FAMILY = "Verdana";
    /** The preview is capped so large sizes stay readable in the window. */
    private static final double MAX_PREVIEW_SIZE = 72;

    @FXML
    private TextField textField;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button validateButton;
    @FXML
    private ComboBox<String> fontPicker;
    @FXML
    private Slider fontSize;
    @FXML
    private Label label;
    @FXML
    private ToggleButton italic;
    @FXML
    private ToggleButton bold;

    private double x;
    private double y;
    private TextLayer result;

    @FXML
    private void initialize() {
        fontPicker.setItems(FXCollections.observableArrayList(Font.getFamilies()));
        fontPicker.setValue(Font.getFamilies().contains(DEFAULT_FAMILY) ? DEFAULT_FAMILY : Font.getDefault().getFamily());

        InvalidationListener preview = obs -> updatePreview();
        textField.textProperty().addListener(preview);
        fontPicker.valueProperty().addListener(preview);
        fontSize.valueProperty().addListener(preview);
        colorPicker.valueProperty().addListener(preview);
        bold.selectedProperty().addListener(preview);
        italic.selectedProperty().addListener(preview);
        validateButton.disableProperty().bind(textField.textProperty().isEmpty());
        updatePreview();
    }

    /** @param x, y where the text baseline will start on the canvas */
    public void init(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Optional<TextLayer> getResult() {
        return Optional.ofNullable(result);
    }

    private void updatePreview() {
        label.setText(textField.getText().isEmpty() ? I18n.get("text.sample") : textField.getText());
        label.setFont(Font.font(fontPicker.getValue(),
                bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL,
                italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR,
                Math.min(fontSize.getValue(), MAX_PREVIEW_SIZE)));
        label.setTextFill(colorPicker.getValue());
    }

    @FXML
    private void validate() {
        if (textField.getText().isEmpty()) {
            return;
        }
        result = new TextLayer(x, y, textField.getText(), fontPicker.getValue(), bold.isSelected(),
                italic.isSelected(), fontSize.getValue(), colorPicker.getValue());
        close();
    }

    @FXML
    private void cancel() {
        result = null;
        close();
    }

    private void close() {
        textField.getScene().getWindow().hide();
    }
}
