package com.mobipic;

import javafx.application.Platform;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Starts the JavaFX toolkit once for tests that need fonts, images or snapshots.
 */
public final class FxToolkit {

    private static boolean started;

    private FxToolkit() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // fine
        }
        Platform.setImplicitExit(false);
        started = true;
    }

    /** Runs the task on the JavaFX application thread and waits for its result. */
    public static <T> T onFx(Callable<T> task) throws Exception {
        start();
        CompletableFuture<T> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw e;
        }
    }

    /** @return an image filled with a single colour */
    public static WritableImage solidImage(int width, int height, Color color) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                writer.setColor(x, y, color);
            }
        }
        return image;
    }
}
