package com.mobipic.io;

import com.mobipic.FxToolkit;
import com.mobipic.model.ColorFilter;
import com.mobipic.model.EllipseLayer;
import com.mobipic.model.FillMode;
import com.mobipic.model.ImageLayer;
import com.mobipic.model.Layer;
import com.mobipic.model.LineLayer;
import com.mobipic.model.PathLayer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TextStyle;
import com.mobipic.model.TriangleLayer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectIOTest {

    private static final ShapeStyle STYLE = new ShapeStyle(Color.web("#11223380"), Color.web("#aabbccff"), 3.5,
            FillMode.OUTLINE_AND_FILL);
    private static final int SAMPLE_LAYERS = 10;

    @TempDir
    Path dir;

    @BeforeAll
    static void startFx() {
        FxToolkit.start();
    }

    private static ProjectModel sampleProject() {
        ProjectModel model = new ProjectModel(FxToolkit.solidImage(120, 80, Color.BLUE));
        model.getBase().setFilter(ColorFilter.GREEN);
        model.getBase().setName("Photo");
        ImageLayer overlay = new ImageLayer(FxToolkit.solidImage(10, 10, Color.RED), 5, 6, 30, 40);
        overlay.setFilter(ColorFilter.PINK);
        overlay.setRotation(30);
        overlay.setOpacity(0.5);
        model.add(overlay);
        model.duplicate(overlay); // shares the same Image
        model.add(new TextLayer(10, 50, "Bonjour \"Mobipic\" é\nligne 2",
                new TextStyle("Verdana", 24, true, true, TextAlignment.CENTER, Color.ORANGE, Color.BLACK, 2, true)));
        RectangleLayer hidden = new RectangleLayer(1, 2, 3, 3, STYLE);
        hidden.setVisible(false);
        model.add(hidden);
        model.add(new RectangleLayer(4, 5, 6, 7, 2.5, STYLE));
        model.add(new EllipseLayer(8, 9, 10, 20, STYLE));
        model.add(new LineLayer(15, 16, 17, 18, STYLE.withFillMode(FillMode.OUTLINE)));
        model.add(new TriangleLayer(19, 20, 21, 22, 23, 24, STYLE));
        model.add(new PathLayer(new double[]{1, 2, 3}, new double[]{4, 5, 6}, STYLE));
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

    /** A project saved by Mobipic 2.0 (format 1). */
    @Test
    void format1ProjectsStillOpen() throws IOException {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(FxToolkit.solidImage(50, 40, Color.WHITE), null), "png", png);
        String json = """
                {"formatVersion":1,"application":"Mobipic","width":50,"height":40,"layers":[
                  {"type":"image","x":0,"y":0,"width":50,"height":40,"image":"images/0.png","filter":"RED"},
                  {"type":"square","x":1,"y":2,"side":10,"color":"#ff0000ff","lineWidth":2,"filled":true},
                  {"type":"circle","x":3,"y":4,"radius":5,"color":"#00ff00ff","lineWidth":1,"filled":false},
                  {"type":"rectangle","x":5,"y":6,"width":7,"height":8,"color":"#0000ffff","lineWidth":3,"filled":false},
                  {"type":"ellipse","x":1,"y":1,"radiusX":4,"radiusY":2,"color":"#000000ff","lineWidth":1,"filled":true},
                  {"type":"line","x1":0,"y1":0,"x2":9,"y2":9,"color":"#123456ff","lineWidth":4},
                  {"type":"triangle","x1":0,"y1":0,"x2":5,"y2":0,"x3":0,"y3":5,"color":"#ffffffff","lineWidth":1,"filled":false},
                  {"type":"text","x":10,"y":30,"text":"Salut","family":"Verdana","bold":false,"italic":false,"size":20,"color":"#000000ff"}
                ]}
                """;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("project.json"));
            zip.write(json.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("images/0.png"));
            zip.write(png.toByteArray());
            zip.closeEntry();
        }

        ProjectModel model = ProjectIO.read(new ByteArrayInputStream(out.toByteArray()));
        List<Layer> layers = model.getLayers();
        assertEquals(8, layers.size());
        assertEquals(ColorFilter.RED, model.getBase().getFilter());

        RectangleLayer square = assertInstanceOf(RectangleLayer.class, layers.get(1));
        assertEquals(10, square.getWidth());
        assertEquals(FillMode.FILL, square.getStyle().fillMode());
        assertEquals(Color.RED, square.getStyle().fill());

        EllipseLayer circle = assertInstanceOf(EllipseLayer.class, layers.get(2));
        assertEquals(10, circle.getWidth());
        assertEquals(FillMode.OUTLINE, circle.getStyle().fillMode());

        EllipseLayer ellipse = assertInstanceOf(EllipseLayer.class, layers.get(4));
        assertEquals(8, ellipse.getWidth());
        assertEquals(4, ellipse.getHeight());

        assertEquals(4, assertInstanceOf(LineLayer.class, layers.get(5)).getStyle().lineWidth());

        // Format 1 texts were placed by their baseline: the block now starts above it
        TextLayer text = assertInstanceOf(TextLayer.class, layers.get(7));
        assertTrue(text.getY() < 30);
        assertTrue(text.bounds().maxY() > 30);
        assertTrue(layers.stream().allMatch(l -> l.isVisible() && l.getOpacity() == 1 && l.getName() == null));
    }

    @Test
    void savingOverAnExistingFileReplacesIt() throws IOException {
        Path file = dir.resolve("p.mbpc");
        Files.writeString(file, "old content");
        ProjectIO.save(sampleProject(), file);
        assertEquals(SAMPLE_LAYERS, ProjectIO.load(file).getLayers().size());
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
        assertEquals(SAMPLE_LAYERS, ProjectIO.load(moved).getLayers().size());
    }

    @Test
    void invalidFilesAreRejected() throws IOException {
        Path notAZip = Files.writeString(dir.resolve("bad.mbpc"), "hello");
        assertThrows(IOException.class, () -> ProjectIO.load(notAZip));

        Path missingImage = dir.resolve("missing.mbpc");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(missingImage))) {
            zip.putNextEntry(new ZipEntry("project.json"));
            zip.write("""
                    {"formatVersion":2,"width":10,"height":10,"layers":[
                      {"type":"image","x":0,"y":0,"width":10,"height":10,"image":"images/0.png","filter":"NONE"}]}
                    """.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        assertThrows(IOException.class, () -> ProjectIO.load(missingImage));
    }

    @Test
    void newerFormatIsRejected() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("project.json"));
            zip.write("{\"formatVersion\":99,\"width\":1,\"height\":1,\"layers\":[]}".getBytes(StandardCharsets.UTF_8));
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

    /** @return a comparable description of every layer (type, geometry, style and common properties) */
    private static List<String> describe(ProjectModel model) {
        List<String> result = new ArrayList<>();
        for (Layer layer : model.getLayers()) {
            String common = " [" + layer.getName() + " " + layer.isVisible() + " " + layer.getOpacity() + " "
                    + layer.getRotation() + "]";
            String d = switch (layer) {
                case ImageLayer l -> "image " + l.bounds() + " " + l.getFilter() + " " + size(l.getImage());
                case TextLayer l -> "text " + l.getX() + "," + l.getY() + " " + l.getText() + " " + l.getStyle();
                case RectangleLayer l -> "rect " + l.bounds() + " " + l.getCornerRadius() + " " + l.getStyle();
                case EllipseLayer l -> "ellipse " + l.bounds() + " " + l.getStyle();
                case LineLayer l -> "line " + l.handles() + " " + l.getStyle();
                case TriangleLayer l -> "triangle " + l.handles() + " " + l.getStyle();
                case PathLayer l -> "path " + Arrays.toString(l.getXs()) + Arrays.toString(l.getYs()) + " " + l.getStyle();
            };
            result.add(d + common);
        }
        return result;
    }

    private static String size(Image image) {
        return (int) image.getWidth() + "x" + (int) image.getHeight();
    }
}
