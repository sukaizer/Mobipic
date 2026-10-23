package com.mobipic.controller;

import com.mobipic.tool.ToolKind;
import com.mobipic.tool.ToolSettings;
import com.mobipic.util.I18n;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.Map;

/**
 * The tool column on the left of the canvas: tools, primary / secondary colours and a colour palette.
 * Left-clicking a palette colour sets the primary colour, right-clicking sets the secondary one.
 */
public final class ToolPaletteController {

    /** The classic Paint palette. */
    private static final List<String> PALETTE = List.of(
            "#000000", "#7f7f7f", "#ffffff", "#c3c3c3", "#880015", "#b97a57",
            "#ed1c24", "#ffaec9", "#ff7f27", "#ffc90e", "#fff200", "#efe4b0",
            "#22b14c", "#b5e61d", "#00a2e8", "#99d9ea", "#3f48cc", "#7092be",
            "#a349a4", "#c8bfe7");
    private static final double SWATCH_SIZE = 16;

    @FXML
    private ToggleGroup toolGroup;
    @FXML
    private ToggleButton selectTool;
    @FXML
    private ToggleButton brushTool;
    @FXML
    private ToggleButton rectangleTool;
    @FXML
    private ToggleButton roundedRectangleTool;
    @FXML
    private ToggleButton ellipseTool;
    @FXML
    private ToggleButton triangleTool;
    @FXML
    private ToggleButton lineTool;
    @FXML
    private ToggleButton textTool;
    @FXML
    private ToggleButton eyedropperTool;
    @FXML
    private Pane colorIndicator;
    @FXML
    private Rectangle primarySwatch;
    @FXML
    private Rectangle secondarySwatch;
    @FXML
    private TilePane palette;

    private ToolSettings settings;

    @FXML
    private void initialize() {
        Map.of(selectTool, ToolKind.SELECT, brushTool, ToolKind.BRUSH, rectangleTool, ToolKind.RECTANGLE,
                roundedRectangleTool, ToolKind.ROUNDED_RECTANGLE, ellipseTool, ToolKind.ELLIPSE,
                triangleTool, ToolKind.TRIANGLE, lineTool, ToolKind.LINE, textTool, ToolKind.TEXT,
                eyedropperTool, ToolKind.EYEDROPPER).forEach(Toggle::setUserData);
        for (String hex : PALETTE) {
            Rectangle swatch = new Rectangle(SWATCH_SIZE, SWATCH_SIZE, Color.web(hex));
            swatch.getStyleClass().add("palette-swatch");
            Tooltip.install(swatch, new Tooltip(I18n.get("colors.swatchHelp")));
            swatch.setOnMouseClicked(e -> {
                if (settings != null) {
                    (e.getButton() == MouseButton.SECONDARY ? settings.secondaryProperty() : settings.primaryProperty())
                            .set((Color) swatch.getFill());
                }
            });
            palette.getChildren().add(swatch);
        }
    }

    /** Connects the palette to the shared settings and to the active tool, in both directions. */
    public void init(ToolSettings settings, ObjectProperty<ToolKind> toolKind) {
        this.settings = settings;
        primarySwatch.fillProperty().bind(settings.primaryProperty());
        secondarySwatch.fillProperty().bind(settings.secondaryProperty());
        Tooltip.install(colorIndicator, new Tooltip(I18n.get("colors.indicator")));

        selectToggle(toolKind.get());
        toolKind.addListener((obs, old, kind) -> selectToggle(kind));
        toolGroup.selectedToggleProperty().addListener((obs, old, toggle) -> {
            if (toggle == null) {
                old.setSelected(true); // a tool is always active
            } else {
                toolKind.set((ToolKind) toggle.getUserData());
            }
        });
    }

    private void selectToggle(ToolKind kind) {
        toolGroup.getToggles().stream()
                .filter(t -> t.getUserData() == kind)
                .findFirst()
                .ifPresent(t -> t.setSelected(true));
    }

    @FXML
    private void swapColors() {
        settings.swapColors();
    }
}
