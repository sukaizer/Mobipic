package com.mobipic.controller;

import com.mobipic.FxToolkit;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.TextLayer;
import com.mobipic.tool.ToolKind;
import com.mobipic.util.Fxml;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the real main window with simulated mouse and keyboard events, dispatched like real input
 * (to the node under the pointer, to the focused node), to catch problems that calling the tools directly misses.
 */
class CanvasInteractionTest {

    private Stage stage;
    private ProjectModel model;
    private CanvasController canvas;

    @BeforeEach
    void open() throws Exception {
        FxToolkit.onFx(() -> {
            Fxml.View<MainController> view = Fxml.load("main.fxml");
            stage = new Stage();
            stage.setScene(new Scene(view.root(), 1200, 760));
            stage.show();
            view.controller().init(stage, null);
            model = new ProjectModel(FxToolkit.solidImage(800, 500, Color.WHITE));
            Field project = MainController.class.getDeclaredField("project");
            project.setAccessible(true);
            @SuppressWarnings("unchecked")
            var property = (javafx.beans.property.ObjectProperty<ProjectModel>) project.get(view.controller());
            property.set(model);
            Field c = MainController.class.getDeclaredField("canvasViewController");
            c.setAccessible(true);
            canvas = (CanvasController) c.get(view.controller());
            return null;
        });
        settle();
    }

    @AfterEach
    void close() throws Exception {
        FxToolkit.onFx(() -> {
            stage.close();
            return null;
        });
    }

    /** Regression: the canvas ScrollPane took the keyboard focus on click, so typed text went nowhere. */
    @Test
    void typingTextWithTheTextToolAndTheOptionsBar() throws Exception {
        fx(() -> key(KeyCode.T, "t"));
        assertEquals(ToolKind.TEXT, canvas.toolKindProperty().get());

        fx(() -> clickImage(100, 100));
        assertTrue(canvas.isEditingText());
        assertInstanceOf(TextArea.class, focusOwner(), "the text box must have the keyboard focus");

        fx(() -> type("Hello"));
        TextLayer text = assertInstanceOf(TextLayer.class, model.getSelected());
        assertEquals("Hello", text.getText());

        // Changing an option sends the focus back to the text, so typing simply continues
        fx(() -> clickNode(((Spinner<?>) root().lookup("#sizeSpinner")).lookup(".increment-arrow-button")));
        assertEquals(37, text.getStyle().size());
        assertInstanceOf(TextArea.class, focusOwner());
        fx(() -> type(" world"));
        assertEquals("Hello world", text.getText());

        fx(() -> key(KeyCode.ESCAPE, ""));
        assertFalse(canvas.isEditingText());
        assertEquals("Hello world", text.getText());
        assertEquals(2, model.getLayers().size());

        // Clicking the text with the text tool edits it again
        fx(() -> clickImage(text.bounds().x() + 3, text.bounds().y() + text.bounds().h() / 2));
        assertTrue(canvas.isEditingText());
    }

    /** Regression: the selection frame was painted over the shape being drawn. */
    @Test
    void shapesBeingDrawnAreNotCoveredByTheSelectionFrame() throws Exception {
        fx(() -> {
            canvas.settings().primaryProperty().set(Color.RED);
            canvas.settings().lineWidthProperty().set(6);
            canvas.toolKindProperty().set(ToolKind.RECTANGLE);
            dragImage(100, 100, 300, 250);
            return null;
        });
        RectangleLayer rect = assertInstanceOf(RectangleLayer.class, model.getSelected());
        assertEquals(200, rect.getWidth(), 0.5);
        Color onTheOutline = fx(() -> canvasColorAt(200, 100));
        assertEquals(Color.RED, onTheOutline, "the outline must show the shape colour, not the selection frame");

        // With the selection tool, the frame is shown again
        fx(() -> {
            canvas.toolKindProperty().set(ToolKind.SELECT);
            return null;
        });
        assertFalse(Color.RED.equals(fx(() -> canvasColorAt(200, 100))));
    }

    // ---- Simulated input ------------------------------------------------------------------------

    private interface FxCall<T> {
        T call() throws Exception;
    }

    private interface FxRun {
        void run() throws Exception;
    }

    private <T> T fx(FxCall<T> call) throws Exception {
        T result = FxToolkit.onFx(call::call);
        settle();
        return result;
    }

    private void fx(FxRun run) throws Exception {
        FxToolkit.onFx(() -> {
            run.run();
            return null;
        });
        settle();
    }

    /** Lets queued runLater calls (focus changes...) run. */
    private static void settle() throws Exception {
        Thread.sleep(150);
        FxToolkit.onFx(() -> null);
    }

    private Parent root() {
        return stage.getScene().getRoot();
    }

    private Node focusOwner() throws Exception {
        return FxToolkit.onFx(() -> stage.getScene().getFocusOwner());
    }

    private Color canvasColorAt(double ix, double iy) {
        Canvas c = canvas.getCanvas();
        WritableImage image = c.snapshot(new SnapshotParameters(), null);
        Point2D p = c.parentToLocal(ix, iy); // surface (image) coordinates to canvas pixels
        return image.getPixelReader().getColor((int) Math.round(p.getX()), (int) Math.round(p.getY()));
    }

    private Point2D imageToScene(double ix, double iy) {
        return canvas.getCanvas().getParent().localToScene(ix, iy);
    }

    private void clickImage(double ix, double iy) {
        click(imageToScene(ix, iy));
    }

    private void clickNode(Node node) {
        Bounds b = node.localToScene(node.getBoundsInLocal());
        click(new Point2D(b.getCenterX(), b.getCenterY()));
    }

    private void click(Point2D scene) {
        Node target = pick(root(), scene);
        mouse(target, scene, MouseEvent.MOUSE_PRESSED, true);
        mouse(target, scene, MouseEvent.MOUSE_RELEASED, false);
        mouse(target, scene, MouseEvent.MOUSE_CLICKED, false);
    }

    private void dragImage(double x1, double y1, double x2, double y2) {
        Point2D from = imageToScene(x1, y1);
        Node target = pick(root(), from);
        mouse(target, from, MouseEvent.MOUSE_PRESSED, true);
        for (int i = 1; i <= 5; i++) {
            mouse(target, imageToScene(x1 + (x2 - x1) * i / 5, y1 + (y2 - y1) * i / 5), MouseEvent.MOUSE_DRAGGED, true);
        }
        mouse(target, imageToScene(x2, y2), MouseEvent.MOUSE_RELEASED, false);
    }

    /** The top-most node under the scene point, as JavaFX picking would find it. */
    private static Node pick(Node node, Point2D scene) {
        if (!node.isVisible() || node.isMouseTransparent()) {
            return null;
        }
        if (node instanceof Parent parent) {
            var children = parent.getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node hit = pick(children.get(i), scene);
                if (hit != null) {
                    return hit;
                }
            }
        }
        boolean pickable = node.isPickOnBounds() || node instanceof Canvas
                || node instanceof javafx.scene.shape.Shape || node instanceof javafx.scene.text.Text
                || (node instanceof javafx.scene.layout.Region r && r.getBackground() != null && !r.getBackground().isEmpty());
        return pickable && node.contains(node.sceneToLocal(scene)) ? node : null;
    }

    private static void mouse(Node target, Point2D scene, EventType<MouseEvent> type, boolean buttonDown) {
        Point2D local = target.sceneToLocal(scene);
        Event.fireEvent(target, new MouseEvent(target, target, type, local.getX(), local.getY(), scene.getX(),
                scene.getY(), MouseButton.PRIMARY, 1, false, false, false, false, buttonDown, false, false, true,
                false, true, new PickResult(target, scene.getX(), scene.getY())));
    }

    private void type(String text) {
        for (char ch : text.toCharArray()) {
            Node target = stage.getScene().getFocusOwner();
            Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_TYPED, String.valueOf(ch), "", KeyCode.UNDEFINED,
                    false, false, false, false));
        }
    }

    private void key(KeyCode code, String ch) {
        Node focus = stage.getScene().getFocusOwner();
        Node target = focus == null ? root() : focus;
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, ch, "", code, false, false, false, false));
    }
}
