package com.mobipic.io;

import com.mobipic.FxToolkit;
import com.mobipic.model.CircleLayer;
import com.mobipic.model.ColorFilter;
import com.mobipic.model.EllipseLayer;
import com.mobipic.model.ImageLayer;
import com.mobipic.model.Layer;
import com.mobipic.model.LineLayer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.SquareLayer;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TriangleLayer;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectIOTest {

    private static final ShapeStyle STYLE = new ShapeStyle(Color.web("#11223380"), 3.5, true);

    @TempDir
    Path dir;

    @BeforeAll
    static void startFx() {
        FxToolkit.start();
    }

    private static ProjectModel sampleProject() {
        ProjectModel model = new ProjectModel(FxToolkit.solidImage(120, 80, Color.BLUE));
        model.getBase().setFilter(ColorFilter.GREEN);
        ImageLayer overlay = new ImageLayer(FxToolkit.solidImage(10, 10, Color.RED), 5, 6, 30, 40);
        overlay.setFilter(ColorFilter.PINK);
        model.add(overlay);
        model.duplicate(overlay); // shares the same Image
        model.add(new TextLayer(10, 50, "Bonjour \"Mobipic\" é", "Verdana", true, true, 24, Color.ORANGE));
        model.add(new SquareLayer(1, 2, 3, STYLE));
        model.add(new RectangleLayer(4, 5, 6, 7, STYLE));
        model.add(new CircleLayer(8, 9, 10, STYLE));
        model.add(new EllipseLayer(11, 12, 13, 14, STYLE));
        model.add(new LineLayer(15, 16, 17, 18, new ShapeStyle(Color.BLACK, 1, false)));
        model.add(new TriangleLayer(19, 20, 21, 22, 23, 24, STYLE));
        return model;
    }

    @Test
    void saveThenLoadRestoresEveryLayer() throws IOException {
        ProjectModel original = sampleProject();
        Path file = dir.resolve("projet.mbpc");
        ProjectIO.save(original, file);

        ProjectModel loaded = ProjectIO.load(file);
        assertEquals(original.getWidth(), loaded.getWidth());
        assertEquals(original.getHeight(), loaded.getHeight());
        assertEquals(describe(original), describe(loaded));
        assertFalse(loaded.isDirty());

        ImageLayer base = loaded.getBase();
        assertEquals(120, base.getImage().getWidth());
        assertEquals(Color.BLUE, base.getImage().getPixelReader().getColor(3, 3));
        // The duplicated image is decoded once and shared again
        assertSame(((ImageLayer) loaded.getLayers().get(1)).getImage(),
                ((ImageLayer) loaded.getLayers().get(2)).getImage());
    }

    @Test
    void sharedImagesAreStoredOnce() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProjectIO.write(sampleProject(), out);
        List<String> entries = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }
        assertEquals(List.of("images/0.png", "images/1.png", "project.json"), entries.stream().sorted().toList());
    }

    @Test
    void savingOverAnExistingFileReplacesIt() throws IOException {
        Path file = dir.resolve("p.mbpc");
        Files.writeString(file, "old content");
        ProjectIO.save(sampleProject(), file);
        assertEquals(10, ProjectIO.load(file).getLayers().size());
        try (var files = Files.list(dir)) {
            assertEquals(List.of(file), files.toList()); // no temp file left behind
        }
    }

    @Test
    void projectIsPortable() throws IOException {
        Path file = dir.resolve("a.mbpc");
        ProjectIO.save(sampleProject(), file);
        Path moved = Files.createDirectory(dir.resolve("elsewhere")).resolve("b.mbpc");
        Files.move(file, moved);
        assertEquals(10, ProjectIO.load(moved).getLayers().size());
    }

    @Test
    void invalidFilesAreRejected() throws IOException {
        Path notAZip = Files.writeString(dir.resolve("bad.mbpc"), "hello");
        assertThrows(IOException.class, () -> ProjectIO.load(notAZip));

        Path missingImage = dir.resolve("missing.mbpc");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(missingImage))) {
            zip.putNextEntry(new ZipEntry("project.json"));
            zip.write("""
                    {"formatVersion":1,"width":10,"height":10,"layers":[
                      {"type":"image","x":0,"y":0,"width":10,"height":10,"image":"images/0.png","filter":"NONE"}]}
                    """.getBytes());
            zip.closeEntry();
        }
        assertThrows(IOException.class, () -> ProjectIO.load(missingImage));
    }

    @Test
    void newerFormatIsRejected() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("project.json"));
            zip.write("{\"formatVersion\":99,\"width\":1,\"height\":1,\"layers\":[]}".getBytes());
            zip.closeEntry();
        }
        IOException e = assertThrows(IOException.class,
                () -> ProjectIO.read(new ByteArrayInputStream(out.toByteArray())));
        assertTrue(e.getMessage().contains("99"));
    }

    @Test
    void colorsAreWrittenAsHexWithAlpha() {
        assertEquals("#11223380", ProjectIO.toHex(Color.web("#11223380")));
        assertEquals("#ffffffff", ProjectIO.toHex(Color.WHITE));
    }

    /** @return a comparable description of every layer (type, geometry and style) */
    private static List<String> describe(ProjectModel model) {
        List<String> result = new ArrayList<>();
        for (Layer layer : model.getLayers()) {
            String d = switch (layer) {
                case ImageLayer l -> "image " + l.getX() + "," + l.getY() + " " + l.getWidth() + "x" + l.getHeight()
                        + " " + l.getFilter() + " " + size(l.getImage());
                case TextLayer l -> "text " + l.getX() + "," + l.getY() + " " + l.getText() + " " + l.getFamily()
                        + " " + l.isBold() + l.isItalic() + " " + l.getSize() + " " + l.getColor();
                case SquareLayer l -> "square " + l.getX() + "," + l.getY() + " " + l.getSide() + " " + l.getStyle();
                case RectangleLayer l -> "rect " + l.getX() + "," + l.getY() + " " + l.getWidth() + "x" + l.getHeight()
                        + " " + l.getStyle();
                case CircleLayer l -> "circle " + l.getX() + "," + l.getY() + " " + l.getRadius() + " " + l.getStyle();
                case EllipseLayer l -> "ellipse " + l.getX() + "," + l.getY() + " " + l.getRadiusX() + " "
                        + l.getRadiusY() + " " + l.getStyle();
                case LineLayer l -> "line " + l.handles() + " " + l.getStyle();
                case TriangleLayer l -> "triangle " + l.handles() + " " + l.getStyle();
            };
            result.add(d);
        }
        assertInstanceOf(ImageLayer.class, model.getLayers().getFirst());
        return result;
    }

    private static String size(Image image) {
        return (int) image.getWidth() + "x" + (int) image.getHeight();
    }
}
