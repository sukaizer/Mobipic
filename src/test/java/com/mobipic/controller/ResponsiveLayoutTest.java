package com.mobipic.controller;

import com.mobipic.FxToolkit;
import com.mobipic.model.ProjectModel;
import com.mobipic.tool.ToolKind;
import com.mobipic.util.Fxml;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The main window must stay usable at any size: every bar visible, no overlapping panels, the tool palette
 * keeping its two columns, and the extra space of a large window going to the canvas.
 */
class ResponsiveLayoutTest {

    private static final double EPS = 1;

    @Test
    void smallWindowKeepsEveryPartVisible() throws Exception {
        String problems = inWindow(700, 450, root -> {
            StringBuilder sb = new StringBuilder();
            double width = root.getScene().getWidth();
            double height = root.getScene().getHeight();
            check(sb, bounds(root, ".menu-bar").getMinY() >= -EPS, "menu bar pushed off the top");
            check(sb, bounds(root, ".options-bar").getMinY() >= -EPS, "options bar pushed off the top");
            check(sb, bounds(root, ".options-bar").getMaxX() <= width + EPS, "options bar wider than the window");
            check(sb, bounds(root, ".status-bar").getMaxY() <= height + EPS, "status bar pushed off the bottom");
            check(sb, Math.abs(bounds(root, "#selectTool").getMinY() - bounds(root, "#brushTool").getMinY()) < EPS,
                    "tool palette collapsed to one column");
            check(sb, bounds(root, "#layersList").getMinX() >= bounds(root, ".canvas-scroll").getMaxX() - EPS,
                    "layer panel overlaps the canvas");
            check(sb, bounds(root, "#renameButton").getMaxY() <= bounds(root, ".status-bar").getMinY() + EPS,
                    "layer buttons cut off");
            return sb.toString();
        });
        assertEquals("", problems);
    }

    @Test
    void largeWindowGivesTheSpaceToTheCanvas() throws Exception {
        String problems = inWindow(1920, 1040, root -> {
            StringBuilder sb = new StringBuilder();
            check(sb, bounds(root, "#layersList").getWidth() <= 420 + EPS, "layer panel too wide: "
                    + bounds(root, "#layersList").getWidth());
            ScrollPane canvasScroll = (ScrollPane) root.lookup(".canvas-scroll");
            boolean scrollBars = canvasScroll.lookupAll(".scroll-bar").stream().anyMatch(Node::isVisible);
            check(sb, !scrollBars, "scroll bars shown although the image fits");
            return sb.toString();
        });
        assertEquals("", problems);
    }

    /** Regression: with a big image the toolbar grew wider than the window, scrolled and wrapped. */
    @Test
    void bigImageDoesNotStretchTheBars() throws Exception {
        String problems = inWindow(1000, 650, 6000, 4500, root -> {
            StringBuilder sb = new StringBuilder();
            double width = root.getScene().getWidth();
            check(sb, bounds(root, ".tool-bar").getWidth() <= width + EPS, "toolbar wider than the window: "
                    + bounds(root, ".tool-bar").getWidth());
            check(sb, bounds(root, ".tool-bar").getHeight() < 60, "toolbar on several lines");
            check(sb, bounds(root, ".options-bar").getMaxX() <= width + EPS, "options bar wider than the window");
            check(sb, bounds(root, "#layersList").getMaxX() <= width + EPS, "layer panel pushed out of the window");
            return sb.toString();
        });
        assertEquals("", problems);
    }

    private static void check(StringBuilder sb, boolean ok, String problem) {
        if (!ok) {
            sb.append(problem).append('\n');
        }
    }

    private static Bounds bounds(Parent root, String selector) {
        Node node = root.lookup(selector);
        assertFalse(node == null, "missing node " + selector);
        return node.localToScene(node.getBoundsInLocal());
    }

    private static String inWindow(int width, int height, Function<Parent, String> inspect) throws Exception {
        return inWindow(width, height, 900, 600, inspect);
    }

    /** Opens the main window with a project and the text tool (the widest options), then inspects it. */
    private static String inWindow(int width, int height, int imageWidth, int imageHeight,
                                   Function<Parent, String> inspect) throws Exception {
        Stage stage = FxToolkit.onFx(() -> {
            Fxml.View<MainController> view = Fxml.load("main.fxml");
            Stage s = new Stage();
            s.setScene(new Scene(view.root(), width, height));
            s.show();
            Field project = MainController.class.getDeclaredField("project");
            project.setAccessible(true);
            @SuppressWarnings("unchecked")
            var property = (javafx.beans.property.ObjectProperty<ProjectModel>) project.get(view.controller());
            property.set(new ProjectModel(new javafx.scene.image.WritableImage(imageWidth, imageHeight)));
            Field canvas = MainController.class.getDeclaredField("canvasViewController");
            canvas.setAccessible(true);
            ((CanvasController) canvas.get(view.controller())).toolKindProperty().set(ToolKind.TEXT);
            return s;
        });
        Thread.sleep(700);
        String result = FxToolkit.onFx(() -> inspect.apply(stage.getScene().getRoot()));
        FxToolkit.onFx(() -> {
            stage.close();
            return null;
        });
        assertTrue(result != null);
        return result;
    }
}
