package com.mobipic.controller;

import com.mobipic.FxToolkit;
import com.mobipic.util.Fxml;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests: the canvas area must reach a stable layout. A layout loop made the scroll bars
 * appear and disappear on every frame, which looked like doubled, blinking content.
 */
class MainLayoutTest {

    /** A few changes while the window first lays out are fine; dozens mean a loop. */
    private static final int MAX_TOGGLES = 4;

    @TempDir
    Path dir;

    @Test
    void layoutIsStableWithoutProject() throws Exception {
        assertStable(view -> { });
    }

    @Test
    void layoutIsStableWhenZoomedFarOut() throws Exception {
        Path image = dir.resolve("big.png");
        ImageIO.write(SwingFXUtils.fromFXImage(FxToolkit.solidImage(3000, 2000, Color.CORNFLOWERBLUE), null),
                "png", image.toFile());
        assertStable(view -> {
            view.controller().openFile(image);
            Button zoomOut = (Button) view.root().lookup("#zoomMinus");
            for (int i = 0; i < 9; i++) { // 1.25^-9 ~ 13%
                zoomOut.fire();
            }
        });
    }

    @Test
    void largeImageStillScrolls() throws Exception {
        Path image = dir.resolve("big.png");
        ImageIO.write(SwingFXUtils.fromFXImage(FxToolkit.solidImage(3000, 2000, Color.CORNFLOWERBLUE), null),
                "png", image.toFile());
        Stage stage = FxToolkit.onFx(() -> {
            Fxml.View<MainController> view = Fxml.load("main.fxml");
            Stage s = new Stage();
            s.setScene(new Scene(view.root(), 1000, 650));
            s.show();
            view.controller().openFile(image);
            return s;
        });
        Thread.sleep(500);
        boolean scrollable = FxToolkit.onFx(() -> {
            ScrollPane scrollPane = (ScrollPane) stage.getScene().getRoot().lookup(".canvas-scroll");
            boolean visible = scrollPane.lookupAll(".scroll-bar").stream().allMatch(Node::isVisible);
            stage.close();
            return visible;
        });
        assertTrue(scrollable, "both scroll bars should be shown for an image larger than the window");
    }

    /**
     * Regression: a canvas larger than the GPU texture limit (4096 px by default) cannot be rendered and
     * JavaFX fails on every frame. Large photos must still open and display.
     */
    @Test
    void imagesLargerThanTheTextureLimitOpen() throws Exception {
        Path image = dir.resolve("huge.png");
        java.awt.image.BufferedImage huge = new java.awt.image.BufferedImage(6000, 4500,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = huge.createGraphics();
        g.setColor(java.awt.Color.ORANGE);
        g.fillRect(0, 0, 6000, 4500);
        g.dispose();
        ImageIO.write(huge, "png", image.toFile());

        java.io.ByteArrayOutputStream errors = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(errors, true));
        try {
            Stage stage = FxToolkit.onFx(() -> {
                Fxml.View<MainController> view = Fxml.load("main.fxml");
                Stage s = new Stage();
                s.setScene(new Scene(view.root(), 1000, 650));
                s.show();
                view.controller().openFile(image);
                return s;
            });
            Thread.sleep(1000); // let several frames render
            double[] sizes = FxToolkit.onFx(() -> {
                javafx.scene.canvas.Canvas canvas = (javafx.scene.canvas.Canvas) stage.getScene().getRoot()
                        .lookup(".drawing-canvas");
                javafx.geometry.Bounds shown = canvas.getBoundsInParent();
                stage.close();
                return new double[]{canvas.getWidth(), canvas.getHeight(), shown.getWidth(), shown.getHeight()};
            });
            assertTrue(sizes[0] <= 4096 && sizes[1] <= 4096, "canvas texture too large: " + sizes[0] + "x" + sizes[1]);
            assertEquals(6000, sizes[2], 1.0, "the canvas must still cover the whole image");
            assertEquals(4500, sizes[3], 1.0);
        } finally {
            System.setErr(originalErr);
        }
        assertFalse(errors.toString().contains("Exception"), "rendering errors:\n" + errors);
    }

    private interface Setup {
        void apply(Fxml.View<MainController> view) throws Exception;
    }

    private static void assertStable(Setup setup) throws Exception {
        AtomicInteger toggles = new AtomicInteger();
        Stage stage = FxToolkit.onFx(() -> {
            Fxml.View<MainController> view = Fxml.load("main.fxml");
            Stage s = new Stage();
            s.setScene(new Scene(view.root(), 1000, 650));
            s.show();
            setup.apply(view);
            watchScrollBars(view.root(), toggles);
            return s;
        });
        Thread.sleep(1500); // let the scene render: a layout loop toggles the bars on every pulse
        int count = FxToolkit.onFx(() -> {
            stage.close();
            return toggles.get();
        });
        assertTrue(count <= MAX_TOGGLES, "scroll bars toggled " + count + " times");
    }

    private static void watchScrollBars(Parent root, AtomicInteger toggles) {
        ScrollPane scrollPane = (ScrollPane) root.lookup(".canvas-scroll");
        for (Node node : scrollPane.lookupAll(".scroll-bar")) {
            ((ScrollBar) node).visibleProperty().addListener((obs, was, is) -> toggles.incrementAndGet());
        }
    }
}
