package com.mobipic.model;

import com.mobipic.FxToolkit;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectModelTest {

    private static final ShapeStyle STYLE = ShapeStyle.DEFAULT;

    private ProjectModel model;
    private ImageLayer base;

    @BeforeAll
    static void startFx() {
        FxToolkit.start();
    }

    @BeforeEach
    void setUp() {
        model = new ProjectModel(FxToolkit.solidImage(200, 100, Color.WHITE));
        base = model.getBase();
    }

    @Test
    void newProjectHasOnlyTheBaseImage() {
        assertEquals(200, model.getWidth());
        assertEquals(100, model.getHeight());
        assertEquals(List.of(base), model.getLayers());
        assertFalse(model.isDirty());
        assertFalse(model.canUndoProperty().get());
    }

    @Test
    void baseLayerIsProtected() {
        model.remove(base);
        assertNull(model.duplicate(base));
        assertFalse(model.isEditable(base));
        assertEquals(1, model.getLayers().size());
        assertThrows(UnsupportedOperationException.class, () -> model.getLayers().clear());
    }

    @Test
    void addSelectsAndMarksDirty() {
        SquareLayer square = new SquareLayer(10, 10, 20, STYLE);
        model.add(square);
        assertSame(square, model.getSelected());
        assertTrue(model.isDirty());
        model.markSaved(Path.of("x.mbpc"));
        assertFalse(model.isDirty());
    }

    @Test
    void duplicateInsertsAnOffsetCopyAbove() {
        SquareLayer a = new SquareLayer(10, 10, 20, STYLE);
        SquareLayer b = new SquareLayer(50, 50, 20, STYLE);
        model.add(a);
        model.add(b);
        Layer copy = model.duplicate(a);
        assertEquals(List.of(base, a, copy, b), model.getLayers());
        assertEquals(10 + ProjectModel.DUPLICATE_OFFSET, copy.getX());
        assertSame(copy, model.getSelected());
    }

    @Test
    void duplicatedImageKeepsItsSize() {
        ImageLayer image = new ImageLayer(FxToolkit.solidImage(10, 10, Color.RED), 0, 0);
        model.add(image);
        image.moveHandle(2, 40, 30);
        ImageLayer copy = (ImageLayer) model.duplicate(image);
        assertEquals(40, copy.getWidth());
        assertEquals(30, copy.getHeight());
    }

    @Test
    void reorderingNeverGoesBelowTheBase() {
        SquareLayer a = new SquareLayer(0, 0, 1, STYLE);
        SquareLayer b = new SquareLayer(0, 0, 1, STYLE);
        model.add(a);
        model.add(b);
        assertFalse(model.canSendBackward(a));
        assertTrue(model.canBringForward(a));
        model.bringForward(a);
        assertEquals(List.of(base, b, a), model.getLayers());
        model.sendBackward(a);
        assertEquals(List.of(base, a, b), model.getLayers());
        assertFalse(model.canBringForward(b));
    }

    @Test
    void layerAtReturnsTheTopMostLayer() {
        SquareLayer bottom = new SquareLayer(0, 0, 50, STYLE);
        SquareLayer top = new SquareLayer(25, 25, 50, STYLE);
        model.add(bottom);
        model.add(top);
        assertSame(top, model.layerAt(30, 30));
        assertSame(bottom, model.layerAt(10, 10));
        assertSame(base, model.layerAt(150, 90));
        assertNull(model.layerAt(500, 500));
    }

    @Test
    void undoAndRedoRestoreTheLayerList() {
        SquareLayer square = new SquareLayer(0, 0, 10, STYLE);
        model.add(square);
        model.remove(square);
        assertEquals(1, model.getLayers().size());

        model.undo();
        assertEquals(2, model.getLayers().size());
        model.undo();
        assertEquals(1, model.getLayers().size());
        assertFalse(model.canUndoProperty().get());

        model.redo();
        model.redo();
        assertEquals(1, model.getLayers().size());
        assertFalse(model.canRedoProperty().get());
    }

    @Test
    void undoRevertsAnInPlaceChangeAndKeepsTheSelection() {
        SquareLayer square = new SquareLayer(0, 0, 10, STYLE);
        model.add(square);
        model.checkpoint();
        square.translate(30, 0);
        model.changed();

        model.undo();
        Layer restored = model.getSelected();
        assertEquals(0, restored.getX());
        assertSame(model.getLayers().get(1), restored);
    }

    @Test
    void cancelAddLeavesNoTrace() {
        model.add(new SquareLayer(0, 0, 10, STYLE));
        SquareLayer cancelled = new SquareLayer(0, 0, 0, STYLE);
        model.add(cancelled);
        model.cancelAdd(cancelled);
        assertEquals(2, model.getLayers().size());
        assertNull(model.getSelected());
        model.undo(); // undoes the first add, not the cancelled one
        assertEquals(1, model.getLayers().size());
    }

    @Test
    void historyIsBounded() {
        for (int i = 0; i < 60; i++) {
            model.add(new SquareLayer(i, i, 1, STYLE));
        }
        int undone = 0;
        while (model.canUndoProperty().get()) {
            model.undo();
            undone++;
        }
        assertEquals(50, undone);
    }

    @Test
    void selectingAForeignLayerFails() {
        assertThrows(IllegalArgumentException.class, () -> model.select(new SquareLayer(0, 0, 1, STYLE)));
    }
}
