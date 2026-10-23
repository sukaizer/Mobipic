package com.mobipic.controller;

import com.mobipic.model.ShapeStyle;
import com.mobipic.model.ShapeType;
import com.mobipic.tool.ShapeTool;
import com.mobipic.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.shape.Line;

import java.util.Map;
import java.util.Optional;

/**
 * Lets the user pick a shape and its style before drawing it on the canvas.
 */
public final class ShapeDialogController {

    @FXML
    private ToggleGroup shapeGroup;
    @FXML
    private ToggleButton line;
    @FXML
    private ToggleButton square;
    @FXML
    private ToggleButton rectangle;
    @FXML
    private ToggleButton circle;
    @FXML
    private ToggleButton ellipse;
    @FXML
    private ToggleButton triangle;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private CheckBox fillCheck;
    @FXML
    private Slider slider;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Line widthIndicator;
    @FXML
    private Button validateButton;

    private ShapeTool result;

    @FXML
    private void initialize() {
        Map.of(line, ShapeType.LINE, square, ShapeType.SQUARE, rectangle, ShapeType.RECTANGLE,
                circle, ShapeType.CIRCLE, ellipse, ShapeType.ELLIPSE, triangle, ShapeType.TRIANGLE)
                .forEach(Toggle::setUserData);

        widthIndicator.strokeWidthProperty().bind(slider.valueProperty());
        widthIndicator.strokeProperty().bind(colorPicker.valueProperty());
        validateButton.disableProperty().bind(shapeGroup.selectedToggleProperty().isNull());
        fillCheck.disableProperty().bind(line.selectedProperty()); // lines cannot be filled

        descriptionLabel.setText(I18n.get("shape.hint.choose"));
        shapeGroup.selectedToggleProperty().addListener((obs, old, toggle) ->
                descriptionLabel.setText(toggle == null ? I18n.get("shape.hint.choose")
                        : I18n.get(((ShapeType) toggle.getUserData()).hintKey())));
    }

    /** Pre-fills the dialog with the previously used shape and style. */
    public void init(ShapeType lastType, ShapeStyle lastStyle) {
        colorPicker.setValue(lastStyle.color());
        slider.setValue(lastStyle.lineWidth());
        fillCheck.setSelected(lastStyle.filled());
        if (lastType != null) {
            shapeGroup.getToggles().stream()
                    .filter(t -> t.getUserData() == lastType)
                    .findFirst()
                    .ifPresent(t -> t.setSelected(true));
        }
    }

    public Optional<ShapeTool> getResult() {
        return Optional.ofNullable(result);
    }

    public ShapeType getSelectedType() {
        Toggle toggle = shapeGroup.getSelectedToggle();
        return toggle == null ? null : (ShapeType) toggle.getUserData();
    }

    public ShapeStyle getStyle() {
        return new ShapeStyle(colorPicker.getValue(), slider.getValue(), fillCheck.isSelected() && !line.isSelected());
    }

    @FXML
    private void validate() {
        ShapeType type = getSelectedType();
        if (type != null) {
            result = new ShapeTool(type, getStyle());
            close();
        }
    }

    @FXML
    private void cancel() {
        result = null;
        close();
    }

    private void close() {
        validateButton.getScene().getWindow().hide();
    }
}
