package com.mobipic.io;

import com.mobipic.FxToolkit;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeStyle;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImageExporterTest {

    @TempDir
    Path dir;

    @ParameterizedTest
    @EnumSource(ImageExporter.Format.class)
    void exportsAtNativeSizeInEveryFormat(ImageExporter.Format format) throws Exception {
        Path file = dir.resolve("export." + format.extension());
        FxToolkit.onFx(() -> {
            ProjectModel model = new ProjectModel(FxToolkit.solidImage(64, 48, Color.WHITE));
            model.add(new RectangleLayer(0, 0, 32, 48, new ShapeStyle(Color.RED, 1, true)));
            model.select(null);
            ImageExporter.export(model, format, file);
            return null;
        });

        BufferedImage written = ImageIO.read(file.toFile());
        assertNotNull(written, "file is not a readable image");
        assertEquals(64, written.getWidth());
        assertEquals(48, written.getHeight());
        java.awt.Color left = new java.awt.Color(written.getRGB(10, 24));
        java.awt.Color right = new java.awt.Color(written.getRGB(50, 24));
        // JPEG is lossy: compare loosely
        assertEquals(255, left.getRed(), 20);
        assertEquals(0, left.getGreen(), 30);
        assertEquals(255, right.getGreen(), 20);
    }

    @Test
    void formatIsDetectedFromTheExtension() {
        assertEquals(ImageExporter.Format.JPG, ImageExporter.formatOf(Path.of("a.JPEG")));
        assertEquals(ImageExporter.Format.PNG, ImageExporter.formatOf(Path.of("a.png")));
        assertEquals(ImageExporter.Format.BMP, ImageExporter.formatOf(Path.of("a.bmp")));
        assertNull(ImageExporter.formatOf(Path.of("a.gif")));
    }
}
