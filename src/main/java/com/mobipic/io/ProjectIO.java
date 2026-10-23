package com.mobipic.io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mobipic.io.dto.LayerDto;
import com.mobipic.io.dto.ProjectDto;
import com.mobipic.model.ColorFilter;
import com.mobipic.model.EllipseLayer;
import com.mobipic.model.FillMode;
import com.mobipic.model.ImageLayer;
import com.mobipic.model.Layer;
import com.mobipic.model.LineLayer;
import com.mobipic.model.PathLayer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeLayer;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TextStyle;
import com.mobipic.model.TriangleLayer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

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
 * <p>
 * Format history: 1 = Mobipic 2.0; 2 = per-layer name, visibility, opacity and rotation, separate outline
 * and fill colours, rounded rectangles, brush strokes, multi-line text placed by its top-left corner.
 */
public final class ProjectIO {

    public static final String EXTENSION = "mbpc";
    public static final int FORMAT_VERSION = 2;

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
        Reader reader = new Reader(project.formatVersion(), name -> decoded.computeIfAbsent(name, n -> decodeImage(n, images)));
        List<Layer> layers = new ArrayList<>();
        try {
            for (LayerDto dto : project.layers()) {
                layers.add(reader.fromDto(dto));
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IOException("Corrupted project: " + e.getMessage(), e);
        }
        if (!(layers.getFirst() instanceof ImageLayer)) {
            throw new IOException("The first layer of a project must be an image");
        }
        return new ProjectModel(project.width(), project.height(), layers);
    }

    // ---- Writing ------------------------------------------------------------------------------

    private static LayerDto toDto(Layer layer, Map<Image, String> imageEntries) {
        LayerDto.Props props = new LayerDto.Props(layer.getName(), layer.isVisible(), layer.getOpacity(),
                layer.getRotation());
        return switch (layer) {
            case ImageLayer l -> new LayerDto.Image(props, l.getX(), l.getY(), l.getWidth(), l.getHeight(),
                    imageEntries.computeIfAbsent(l.getImage(), img -> IMAGES_DIR + imageEntries.size() + ".png"),
                    l.getFilter().name());
            case TextLayer l -> {
                TextStyle s = l.getStyle();
                yield new LayerDto.Text(props, l.getX(), l.getY(), l.getText(), s.family(), s.size(), s.bold(),
                        s.italic(), s.alignment().name(), toHex(s.color()), toHex(s.outlineColor()),
                        s.outlineWidth(), s.shadow());
            }
            case RectangleLayer l -> new LayerDto.Rectangle(props, l.getX(), l.getY(), l.getWidth(), l.getHeight(),
                    l.getCornerRadius(), stroke(l), null, null, null);
            case EllipseLayer l -> new LayerDto.Ellipse(props, l.getX(), l.getY(), l.getWidth(), l.getHeight(),
                    null, null, stroke(l), null, null, null);
            case LineLayer l -> new LayerDto.Line(props, l.getX(), l.getY(), l.getX2(), l.getY2(), stroke(l), null, null);
            case TriangleLayer l -> new LayerDto.Triangle(props, l.getX(), l.getY(), l.getX2(), l.getY2(),
                    l.getX3(), l.getY3(), stroke(l), null, null, null);
            case PathLayer l -> new LayerDto.Path(props, l.getXs(), l.getYs(), stroke(l));
        };
    }

    private static LayerDto.Stroke stroke(ShapeLayer layer) {
        ShapeStyle s = layer.getStyle();
        return new LayerDto.Stroke(toHex(s.stroke()), toHex(s.fill()), s.lineWidth(), s.fillMode().name());
    }

    static String toHex(Color c) {
        return String.format(Locale.ROOT, "#%02x%02x%02x%02x",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255), Math.round(c.getOpacity() * 255));
    }

    // ---- Reading ------------------------------------------------------------------------------

    private interface ImageResolver {
        Image resolve(String entryName);
    }

    /** Converts DTOs to layers, handling the differences between format versions. */
    private record Reader(int version, ImageResolver images) {

        Layer fromDto(LayerDto dto) {
            Layer layer = switch (dto) {
                case LayerDto.Image d -> {
                    ImageLayer l = new ImageLayer(images.resolve(d.image()), d.x(), d.y(), d.width(), d.height());
                    l.setFilter(d.filter() == null ? ColorFilter.NONE : ColorFilter.valueOf(d.filter()));
                    yield l;
                }
                case LayerDto.Text d -> text(d);
                case LayerDto.Rectangle d -> new RectangleLayer(d.x(), d.y(), d.width(), d.height(),
                        d.cornerRadius() == null ? 0 : d.cornerRadius(), style(d.style(), d.color(), d.lineWidth(), d.filled()));
                case LayerDto.Ellipse d -> d.width() != null
                        ? new EllipseLayer(d.x(), d.y(), d.width(), d.height(), style(d.style(), d.color(), d.lineWidth(), d.filled()))
                        : new EllipseLayer(d.x(), d.y(), 2 * d.radiusX(), 2 * d.radiusY(), style(null, d.color(), d.lineWidth(), d.filled()));
                case LayerDto.Line d -> new LineLayer(d.x1(), d.y1(), d.x2(), d.y2(), style(d.style(), d.color(), d.lineWidth(), false));
                case LayerDto.Triangle d -> new TriangleLayer(d.x1(), d.y1(), d.x2(), d.y2(), d.x3(), d.y3(),
                        style(d.style(), d.color(), d.lineWidth(), d.filled()));
                case LayerDto.Path d -> new PathLayer(d.xs(), d.ys(), style(d.style(), null, null, null));
                case LayerDto.Square d -> new RectangleLayer(d.x(), d.y(), d.side(), d.side(),
                        style(null, d.color(), d.lineWidth(), d.filled()));
                case LayerDto.Circle d -> new EllipseLayer(d.x(), d.y(), 2 * d.radius(), 2 * d.radius(),
                        style(null, d.color(), d.lineWidth(), d.filled()));
            };
            applyProps(layer, dto.props());
            return layer;
        }

        private TextLayer text(LayerDto.Text d) {
            TextStyle style = new TextStyle(d.family(), d.size(), d.bold(), d.italic(),
                    d.alignment() == null ? TextAlignment.LEFT : TextAlignment.valueOf(d.alignment()),
                    Color.web(d.color()),
                    d.outlineColor() == null ? Color.WHITE : Color.web(d.outlineColor()),
                    d.outlineWidth() == null ? 0 : d.outlineWidth(),
                    d.shadow() != null && d.shadow());
            double y = d.y();
            if (version < 2) {
                // Format 1 placed texts by their baseline; format 2 by their top-left corner
                Text measure = new Text("Ag");
                measure.setFont(style.font());
                measure.setTextOrigin(VPos.BASELINE);
                y += measure.getLayoutBounds().getMinY();
            }
            return new TextLayer(d.x(), y, d.text(), style);
        }

        /** Builds a shape style from the format 2 {@code style}, or from the format 1 single colour. */
        private static ShapeStyle style(LayerDto.Stroke stroke, String legacyColor, Double legacyWidth, Boolean legacyFilled) {
            if (stroke != null) {
                return new ShapeStyle(Color.web(stroke.color()), Color.web(stroke.fill()), stroke.lineWidth(),
                        FillMode.valueOf(stroke.fillMode()));
            }
            Color color = Color.web(legacyColor);
            boolean filled = legacyFilled != null && legacyFilled;
            return new ShapeStyle(color, color, legacyWidth == null ? 1 : legacyWidth,
                    filled ? FillMode.FILL : FillMode.OUTLINE);
        }

        private static void applyProps(Layer layer, LayerDto.Props props) {
            if (props == null) {
                return;
            }
            layer.setName(props.name());
            if (props.visible() != null) {
                layer.setVisible(props.visible());
            }
            if (props.opacity() != null) {
                layer.setOpacity(props.opacity());
            }
            if (props.rotation() != null) {
                layer.setRotation(props.rotation());
            }
        }
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
