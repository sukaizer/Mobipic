package com.mobipic.controller;

import com.mobipic.FxToolkit;
import com.mobipic.model.FillMode;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TextStyle;
import com.mobipic.tool.ToolSettings;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayerStyleSyncTest {

    private ProjectModel model;
    private ToolSettings settings;
    private RectangleLayer rect;

    @BeforeAll
    static void startFx() {
        FxToolkit.start();
    }

    @BeforeEach
    void setUp() {
        model = new ProjectModel(FxToolkit.solidImage(100, 100, Color.WHITE));
        settings = new ToolSettings();
        new LayerStyleSync(settings).setModel(model);
        rect = new RectangleLayer(0, 0, 10, 10, new ShapeStyle(Color.RED, Color.BLUE, 7, FillMode.FILL));
        rect.setOpacity(0.6);
        model.add(rect); // selects it
    }

    @Test
    void selectingALayerShowsItsStyle() {
        assertEquals(Color.RED, settings.primaryProperty().get());
        assertEquals(Color.BLUE, settings.secondaryProperty().get());
        assertEquals(7, settings.lineWidthProperty().get());
        assertEquals(FillMode.FILL, settings.fillModeProperty().get());
        assertEquals(0.6, settings.opacityProperty().get(), 1e-9);
    }

    @Test
    void changingAnOptionRestylesTheSelectedLayerWithUndo() {
        settings.primaryProperty().set(Color.GREEN);
        assertEquals(Color.GREEN, rect.getStyle().stroke());
        assertTrue(model.isDirty());
        model.undo();
        RectangleLayer restored = (RectangleLayer) model.getLayers().get(1);
        assertEquals(Color.RED, restored.getStyle().stroke());
    }

    @Test
    void rapidChangesOfOneOptionAreOneUndoStep() {
        for (int w = 8; w <= 20; w++) {
            settings.lineWidthProperty().set(w);
        }
        assertEquals(20, rect.getStyle().lineWidth());
        model.undo();
        assertEquals(7, ((RectangleLayer) model.getLayers().get(1)).getStyle().lineWidth());
    }

    @Test
    void loadingASelectionDoesNotCreateUndoSteps() {
        model.select(null);
        model.select(rect);
        model.undo(); // only the add
        assertEquals(1, model.getLayers().size());
    }

    @Test
    void textOptionsRestyleATextButKeepItsColours() {
        TextLayer text = new TextLayer(0, 0, "Hi", TextStyle.DEFAULT.withColor(Color.PURPLE));
        model.add(text);
        settings.boldProperty().set(true);
        settings.fontSizeProperty().set(64);
        assertTrue(text.getStyle().bold());
        assertEquals(64, text.getStyle().size());
        assertEquals(Color.PURPLE, text.getStyle().color());
    }

    @Test
    void nothingIsAppliedWithoutAnEditableSelection() {
        model.select(model.getBase());
        settings.primaryProperty().set(Color.ORANGE);
        model.select(null);
        settings.lineWidthProperty().set(1);
        assertEquals(Color.RED, rect.getStyle().stroke());
        assertFalse(rect.getStyle().lineWidth() == 1);
    }
}
