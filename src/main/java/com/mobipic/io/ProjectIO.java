package com.mobipic.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mobipic.io.dto.LayerDto;
import com.mobipic.io.dto.ProjectDto;
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
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Reads and writes Mobipic project files.
 * <p>
 * A {@code .mbpc} file is a zip archive holding {@code project.json} (the layers) and the images
 * used by image layers as PNG entries under {@code images/}. The file is self-contained and can be
 * moved or shared freely.
 */
public final class ProjectIO {

    public static final String EXTENSION = "mbpc";
    public static final int FORMAT_VERSION = 1;

    private static final String PROJECT_ENTRY = "project.json";
    private static final String IMAGES_DIR = "images/";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private ProjectIO() {
    }

    /**
     * Saves the project. The archive is written to a temporary file first, then moved over the target,
     * so an existing project is never left half-written.
     */
    public static void save(ProjectModel model, Path target) throws IOException {
        Path dir = target.toAbsolutePath().getParent();
        Path temp = Files.createTempFile(dir, ".mobipic-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                write(model, out);
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static ProjectModel load(Path source) throws IOException {
        try (InputStream in = Files.newInputStream(source)) {
            return read(in);
        }
    }

    static void write(ProjectModel model, OutputStream out) throws IOException {
        // Duplicated image layers share the same Image: store it once.
        Map<Image, String> imageEntries = new IdentityHashMap<>();
        List<LayerDto> layers = new ArrayList<>();
        for (Layer layer : model.getLayers()) {
            layers.add(toDto(layer, imageEntries));
        }
        ProjectDto project = new ProjectDto(FORMAT_VERSION, "Mobipic", model.getWidth(), model.getHeight(), layers);

        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(PROJECT_ENTRY));
            zip.write(MAPPER.writeValueAsBytes(project));
            zip.closeEntry();
            for (Map.Entry<Image, String> entry : imageEntries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getValue()));
                writePng(entry.getKey(), zip);
                zip.closeEntry();
            }
        }
    }

    static ProjectModel read(InputStream in) throws IOException {
        ProjectDto project = null;
        Map<String, byte[]> images = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(PROJECT_ENTRY)) {
                    project = MAPPER.readValue(zip.readAllBytes(), ProjectDto.class);
                } else if (entry.getName().startsWith(IMAGES_DIR)) {
                    images.put(entry.getName(), zip.readAllBytes());
                }
            }
        }
        if (project == null) {
            throw new IOException("Not a Mobipic project: " + PROJECT_ENTRY + " is missing");
        }
        if (project.formatVersion() > FORMAT_VERSION) {
            throw new IOException("Project format " + project.formatVersion() + " is newer than this version of Mobipic");
        }
        if (project.layers() == null || project.layers().isEmpty()) {
            throw new IOException("The project has no base image");
        }

        Map<String, Image> decoded = new HashMap<>();
        List<Layer> layers = new ArrayList<>();
        try {
            for (LayerDto dto : project.layers()) {
                layers.add(fromDto(dto, name -> decoded.computeIfAbsent(name, n -> decodeImage(n, images))));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IOException("Corrupted project: " + e.getMessage(), e);
        }
        if (!(layers.getFirst() instanceof ImageLayer)) {
            throw new IOException("The first layer of a project must be an image");
        }
        return new ProjectModel(project.width(), project.height(), layers);
    }

    // ---- Mapping ------------------------------------------------------------------------------

    private static LayerDto toDto(Layer layer, Map<Image, String> imageEntries) {
        return switch (layer) {
            case ImageLayer l -> new LayerDto.Image(l.getX(), l.getY(), l.getWidth(), l.getHeight(),
                    imageEntries.computeIfAbsent(l.getImage(), img -> IMAGES_DIR + imageEntries.size() + ".png"),
                    l.getFilter().name());
            case TextLayer l -> new LayerDto.Text(l.getX(), l.getY(), l.getText(), l.getFamily(),
                    l.isBold(), l.isItalic(), l.getSize(), toHex(l.getColor()));
            case SquareLayer l -> new LayerDto.Square(l.getX(), l.getY(), l.getSide(),
                    toHex(l.getColor()), l.getLineWidth(), l.isFilled());
            case RectangleLayer l -> new LayerDto.Rectangle(l.getX(), l.getY(), l.getWidth(), l.getHeight(),
                    toHex(l.getColor()), l.getLineWidth(), l.isFilled());
            case CircleLayer l -> new LayerDto.Circle(l.getX(), l.getY(), l.getRadius(),
                    toHex(l.getColor()), l.getLineWidth(), l.isFilled());
            case EllipseLayer l -> new LayerDto.Ellipse(l.getX(), l.getY(), l.getRadiusX(), l.getRadiusY(),
                    toHex(l.getColor()), l.getLineWidth(), l.isFilled());
            case LineLayer l -> new LayerDto.Line(l.getX(), l.getY(), l.getX2(), l.getY2(),
                    toHex(l.getColor()), l.getLineWidth());
            case TriangleLayer l -> new LayerDto.Triangle(l.getX(), l.getY(), l.getX2(), l.getY2(),
                    l.getX3(), l.getY3(), toHex(l.getColor()), l.getLineWidth(), l.isFilled());
        };
    }

    private interface ImageResolver {
        Image resolve(String entryName);
    }

    private static Layer fromDto(LayerDto dto, ImageResolver images) {
        return switch (dto) {
            case LayerDto.Image d -> {
                ImageLayer layer = new ImageLayer(images.resolve(d.image()), d.x(), d.y(), d.width(), d.height());
                layer.setFilter(d.filter() == null ? ColorFilter.NONE : ColorFilter.valueOf(d.filter()));
                yield layer;
            }
            case LayerDto.Text d -> new TextLayer(d.x(), d.y(), d.text(), d.family(), d.bold(), d.italic(),
                    d.size(), Color.web(d.color()));
            case LayerDto.Square d -> new SquareLayer(d.x(), d.y(), d.side(), style(d.color(), d.lineWidth(), d.filled()));
            case LayerDto.Rectangle d -> new RectangleLayer(d.x(), d.y(), d.width(), d.height(),
                    style(d.color(), d.lineWidth(), d.filled()));
            case LayerDto.Circle d -> new CircleLayer(d.x(), d.y(), d.radius(), style(d.color(), d.lineWidth(), d.filled()));
            case LayerDto.Ellipse d -> new EllipseLayer(d.x(), d.y(), d.radiusX(), d.radiusY(),
                    style(d.color(), d.lineWidth(), d.filled()));
            case LayerDto.Line d -> new LineLayer(d.x1(), d.y1(), d.x2(), d.y2(), style(d.color(), d.lineWidth(), false));
            case LayerDto.Triangle d -> new TriangleLayer(d.x1(), d.y1(), d.x2(), d.y2(), d.x3(), d.y3(),
                    style(d.color(), d.lineWidth(), d.filled()));
        };
    }

    private static ShapeStyle style(String color, double lineWidth, boolean filled) {
        return new ShapeStyle(Color.web(color), lineWidth, filled);
    }

    static String toHex(Color c) {
        return String.format(Locale.ROOT, "#%02x%02x%02x%02x",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255), Math.round(c.getOpacity() * 255));
    }

    // ---- Images -------------------------------------------------------------------------------

    private static void writePng(Image image, OutputStream out) throws IOException {
        // Buffer so a failure does not leave a truncated entry
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (!ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", buffer)) {
            throw new IOException("No PNG writer available");
        }
        buffer.writeTo(out);
    }

    private static Image decodeImage(String name, Map<String, byte[]> images) {
        byte[] bytes = images.get(name);
        if (bytes == null) {
            throw new MissingImageException(name);
        }
        Image image = new Image(new ByteArrayInputStream(bytes));
        if (image.isError()) {
            throw new MissingImageException(name);
        }
        return image;
    }

    /** Unchecked so image decoding can be used in {@code computeIfAbsent}; reported as an IOException. */
    private static final class MissingImageException extends IllegalArgumentException {
        MissingImageException(String name) {
            super("Image missing or unreadable in project: " + name);
        }
    }
}
