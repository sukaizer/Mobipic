package com.mobipic.controller;

import com.mobipic.model.EllipseLayer;
import com.mobipic.model.FillMode;
import com.mobipic.model.ImageLayer;
import com.mobipic.model.Layer;
import com.mobipic.model.LineLayer;
import com.mobipic.model.PathLayer;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TriangleLayer;
import com.mobipic.tool.ToolKind;
import com.mobipic.tool.ToolSettings;
import com.mobipic.util.I18n;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * The options bar under the toolbar. It edits the {@link ToolSettings}: the options of the active tool,
 * or, with the selection tool, those of the selected layer (see {@link LayerStyleSync}).
 */
public final class OptionsBarController {

    /** What the bar is showing options for. */
    enum Context { NONE, CLOSED_SHAPE, RECTANGLE, OPEN_SHAPE, BRUSH, TEXT, IMAGE, EYEDROPPER }

    @FXML
    private Label titleLabel;
    @FXML
    private HBox primaryGroup;
    @FXML
    private Label primaryLabel;
    @FXML
    private ColorPicker primaryPicker;
    @FXML
    private HBox secondaryGroup;
    @FXML
    private Label secondaryLabel;
    @FXML
    private ColorPicker secondaryPicker;
    @FXML
    private HBox fillGroup;
    @FXML
    private ChoiceBox<FillMode> fillModeChoice;
    @FXML
    private HBox widthGroup;
    @FXML
    private Spinner<Double> widthSpinner;
    @FXML
    private HBox radiusGroup;
    @FXML
    private Spinner<Double> radiusSpinner;
    @FXML
    private HBox textGroup;
    @FXML
    private ComboBox<String> fontCombo;
    @FXML
    private Spinner<Double> sizeSpinner;
    @FXML
    private ToggleButton boldToggle;
    @FXML
    private ToggleButton italicToggle;
    @FXML
    private ToggleGroup alignGroup;
    @FXML
    private ToggleButton alignLeft;
    @FXML
    private ToggleButton alignCenter;
    @FXML
    private ToggleButton alignRight;
    @FXML
    private Spinner<Double> outlineSpinner;
    @FXML
    private CheckBox shadowCheck;
    @FXML
    private HBox opacityGroup;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Label opacityLabel;
    @FXML
    private Label emptyLabel;

    /**
     * Object views of the numeric settings bound to the spinners. Bidirectional bindings only keep weak
     * references, so these must be held here or they would be garbage collected and the spinners would stop updating.
     */
    private final List<ObjectProperty<Double>> spinnerBindings = new ArrayList<>();

    @FXML
    private void initialize() {
        fillModeChoice.setItems(FXCollections.observableArrayList(FillMode.values()));
        fillModeChoice.setConverter(new StringConverter<>() {
            @Override
            public String toString(FillMode mode) {
                return mode == null ? "" : I18n.get(mode.key());
            }

            @Override
            public FillMode fromString(String s) {
                throw new UnsupportedOperationException();
            }
        });
        fontCombo.setItems(FXCollections.observableArrayList(Font.getFamilies()));
        alignLeft.setUserData(TextAlignment.LEFT);
        alignCenter.setUserData(TextAlignment.CENTER);
        alignRight.setUserData(TextAlignment.RIGHT);
        show(Context.NONE, null);
    }

    public void init(ToolSettings settings) {
        primaryPicker.valueProperty().bindBidirectional(settings.primaryProperty());
        secondaryPicker.valueProperty().bindBidirectional(settings.secondaryProperty());
        fillModeChoice.valueProperty().bindBidirectional(settings.fillModeProperty());
        bindSpinner(widthSpinner, settings.lineWidthProperty(), 0.5, 100, 0.5);
        bindSpinner(radiusSpinner, settings.cornerRadiusProperty(), 0, 500, 1);
        bindSpinner(sizeSpinner, settings.fontSizeProperty(), 1, 500, 1);
        bindSpinner(outlineSpinner, settings.textOutlineProperty(), 0, 50, 0.5);
        fontCombo.valueProperty().bindBidirectional(settings.fontFamilyProperty());
        boldToggle.selectedProperty().bindBidirectional(settings.boldProperty());
        italicToggle.selectedProperty().bindBidirectional(settings.italicProperty());
        shadowCheck.selectedProperty().bindBidirectional(settings.shadowProperty());

        selectAlignment(settings.alignmentProperty().get());
        settings.alignmentProperty().addListener((obs, old, alignment) -> selectAlignment(alignment));
        alignGroup.selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle == null) {
                old.setSelected(true);
            } else {
                settings.alignmentProperty().set((TextAlignment) toggle.getUserData());
            }
        });

        // The slider shows a percentage; the setting is a 0..1 factor
        opacitySlider.setValue(settings.opacityProperty().get() * 100);
        settings.opacityProperty().addListener((obs, old, value) -> opacitySlider.setValue(value.doubleValue() * 100));
        opacitySlider.valueProperty().addListener((obs, old, value) -> {
            settings.opacityProperty().set(value.doubleValue() / 100);
            opacityLabel.setText(Math.round(value.doubleValue()) + " %");
        });
        opacityLabel.setText(Math.round(opacitySlider.getValue()) + " %");
    }

    private void selectAlignment(TextAlignment alignment) {
        alignGroup.getToggles().stream()
                .filter(t -> t.getUserData() == alignment)
                .findFirst()
                .ifPresent(t -> t.setSelected(true));
    }

    private void bindSpinner(Spinner<Double> spinner, DoubleProperty property, double min, double max, double step) {
        SpinnerValueFactory.DoubleSpinnerValueFactory factory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, property.get(), step);
        spinner.setValueFactory(factory);
        ObjectProperty<Double> value = property.asObject();
        spinnerBindings.add(value);
        factory.valueProperty().bindBidirectional(value);
        // Commit typed values when leaving the field, not only on Enter
        spinner.focusedProperty().addListener((obs, was, focused) -> {
            if (!focused) {
                commitEditorText(spinner);
            }
        });
    }

    private static void commitEditorText(Spinner<Double> spinner) {
        try {
            String text = spinner.getEditor().getText().replace(',', '.').trim();
            spinner.getValueFactory().setValue(Double.parseDouble(text));
        } catch (NumberFormatException e) {
            spinner.getEditor().setText(spinner.getValueFactory().getConverter().toString(spinner.getValue()));
        }
    }

    /** Shows the options relevant to the active tool, or to the selected layer when the selection tool is active. */
    public void update(ToolKind tool, Layer selected, boolean editable) {
        if (tool != ToolKind.SELECT) {
            show(contextOf(tool), I18n.get(tool.key()));
        } else if (editable) {
            show(contextOf(selected), I18n.get(selected.typeKey()));
        } else {
            show(Context.NONE, null);
        }
    }

    private static Context contextOf(ToolKind tool) {
        return switch (tool) {
            case RECTANGLE, ROUNDED_RECTANGLE -> Context.RECTANGLE;
            case ELLIPSE, TRIANGLE -> Context.CLOSED_SHAPE;
            case LINE -> Context.OPEN_SHAPE;
            case BRUSH -> Context.BRUSH;
            case TEXT -> Context.TEXT;
            case EYEDROPPER -> Context.EYEDROPPER;
            case SELECT -> Context.NONE;
        };
    }

    private static Context contextOf(Layer layer) {
        return switch (layer) {
            case RectangleLayer r -> Context.RECTANGLE;
            case EllipseLayer e -> Context.CLOSED_SHAPE;
            case TriangleLayer t -> Context.CLOSED_SHAPE;
            case LineLayer l -> Context.OPEN_SHAPE;
            case PathLayer p -> Context.BRUSH;
            case TextLayer t -> Context.TEXT;
            case ImageLayer i -> Context.IMAGE;
        };
    }

    private void show(Context context, String title) {
        boolean shape = context == Context.CLOSED_SHAPE || context == Context.RECTANGLE;
        boolean stroked = shape || context == Context.OPEN_SHAPE || context == Context.BRUSH;
        setShown(primaryGroup, stroked || context == Context.TEXT || context == Context.EYEDROPPER);
        setShown(secondaryGroup, shape || context == Context.TEXT || context == Context.EYEDROPPER);
        setShown(fillGroup, shape);
        setShown(widthGroup, stroked);
        setShown(radiusGroup, context == Context.RECTANGLE);
        setShown(textGroup, context == Context.TEXT);
        setShown(opacityGroup, context != Context.NONE && context != Context.EYEDROPPER);
        setShown(emptyLabel, context == Context.NONE);
        setShown(titleLabel, title != null);
        titleLabel.setText(title == null ? "" : title);

        primaryLabel.setText(I18n.get(switch (context) {
            case CLOSED_SHAPE, RECTANGLE, OPEN_SHAPE -> "options.outline";
            case EYEDROPPER -> "colors.primary";
            default -> "options.color";
        }));
        secondaryLabel.setText(I18n.get(switch (context) {
            case TEXT -> "options.textOutlineColor";
            case EYEDROPPER -> "colors.secondary";
            default -> "options.fill";
        }));
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }
}
