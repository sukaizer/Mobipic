package com.mobipic.io;

import com.mobipic.model.ProjectModel;
import com.mobipic.render.CanvasRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Flattens a project into a bitmap file. Must be called on the JavaFX application thread.
 */
public final class ImageExporter {

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
     * Renders the layers at 100% (independent of the zoom and of the selection) and writes the image.
     */
    public static void export(ProjectModel model, Format format, Path target) throws IOException {
        BufferedImage image = SwingFXUtils.fromFXImage(render(model), null);
        if (!format.supportsAlpha) {
            image = flatten(image);
        }
        // ImageIO silently returns false (instead of failing) when no writer accepts the image
        if (!ImageIO.write(image, format.extension(), target.toFile())) {
            throw new IOException("No " + format.name() + " writer for this image");
        }
    }

    /** @return the project rendered at its native size */
    public static WritableImage render(ProjectModel model) {
        int width = (int) Math.ceil(model.getWidth());
        int height = (int) Math.ceil(model.getHeight());
        Canvas canvas = new Canvas(width, height);
        CanvasRenderer.paintLayers(canvas, model.getLayers());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, new WritableImage(width, height));
    }

    /** Converts an ARGB image to RGB on a white background, as required by JPEG and BMP writers. */
    private static BufferedImage flatten(BufferedImage argb) {
        BufferedImage rgb = new BufferedImage(argb.getWidth(), argb.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(argb, 0, 0, null);
        } finally {
            g.dispose();
        }
        return rgb;
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
