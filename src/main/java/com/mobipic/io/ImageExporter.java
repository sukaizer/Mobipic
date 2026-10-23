package com.mobipic.io;

import com.mobipic.model.ProjectModel;
import com.mobipic.render.CanvasRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Flattens a project into a bitmap. Must be called on the JavaFX application thread.
 * <p>
 * Rendering goes through tiles: a JavaFX canvas cannot be larger than the GPU texture limit (4096 px by
 * default), but projects can be much larger than that.
 */
public final class ImageExporter {

    /** Tile size, safely below the texture limit. */
    static final int TILE_SIZE = 2048;

    /** Supported export formats, matching the "Export as" menu. */
    public enum Format {
        JPG("jpg", false), PNG("png", true), BMP("bmp", false);

        private final String extension;
        private final boolean supportsAlpha;

        Format(String extension, boolean supportsAlpha) {
            this.extension = extension;
            this.supportsAlpha = supportsAlpha;
        }

        public String extension() {
            return extension;
        }
    }

    private ImageExporter() {
    }

    /**
     * Renders the visible layers at 100% (independent of the zoom and of the selection) and writes the image.
     */
    public static void export(ProjectModel model, Format format, Path target) throws IOException {
        BufferedImage image = render(model, format.supportsAlpha);
        // ImageIO silently returns false (instead of failing) when no writer accepts the image
        if (!ImageIO.write(image, format.extension(), target.toFile())) {
            throw new IOException("No " + format.name() + " writer for this image");
        }
    }

    /**
     * @param alpha keep transparency; otherwise the image is flattened on white, as JPEG and BMP require
     * @return the project rendered at its native size
     */
    public static BufferedImage render(ProjectModel model, boolean alpha) {
        int width = (int) Math.ceil(model.getWidth());
        int height = (int) Math.ceil(model.getHeight());
        BufferedImage result = new BufferedImage(width, height,
                alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        try {
            if (!alpha) {
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, width, height);
            }
            for (int y = 0; y < height; y += TILE_SIZE) {
                for (int x = 0; x < width; x += TILE_SIZE) {
                    int w = Math.min(TILE_SIZE, width - x);
                    int h = Math.min(TILE_SIZE, height - y);
                    g.drawImage(SwingFXUtils.fromFXImage(renderRegion(model, x, y, w, h), null), x, y, null);
                }
            }
        } finally {
            g.dispose();
        }
        return result;
    }

    /** @return the given region of the project, at 100%, with a transparent background */
    public static WritableImage renderRegion(ProjectModel model, int x, int y, int width, int height) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.translate(-x, -y);
        CanvasRenderer.paintLayers(gc, model.getLayers(), null);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, new WritableImage(width, height));
    }

    /** @return the format matching the file extension, or {@code null} */
    public static Format formatOf(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return Format.JPG;
        }
        if (name.endsWith(".png")) {
            return Format.PNG;
        }
        if (name.endsWith(".bmp")) {
            return Format.BMP;
        }
        return null;
    }
}
