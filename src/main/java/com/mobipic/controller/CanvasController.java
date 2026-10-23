package com.mobipic.controller;

import com.mobipic.io.ImageExporter;
import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.TextLayer;
import com.mobipic.render.CanvasRenderer;
import com.mobipic.tool.Tool;
import com.mobipic.tool.ToolContext;
import com.mobipic.tool.ToolEvent;
import com.mobipic.tool.ToolKind;
import com.mobipic.tool.ToolSettings;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;

import java.util.Locale;

/**
 * Hosts the drawing canvas: forwards mouse events to the active tool, repaints on model changes and
 * shows a text box on top of the canvas while a text layer is being typed.
 * <p>
 * The canvas is not as large as the image: it is rendered at the resolution it is shown at (zoom times the
 * screen scaling), never larger than the GPU texture limit, then scaled back to the image size. Large
 * photos can therefore be opened, and zoomed-out views repaint faster. Tools always work in image coordinates.
 */
public final class CanvasController implements ToolContext {

    public static final double MIN_ZOOM = 0.1;
    public static final double MAX_ZOOM = 8;

    /** Largest canvas JavaFX can render: its texture limit, 4096 px unless -Dprism.maxTextureSize says otherwise. */
    static final int MAX_CANVAS_SIZE = maxCanvasSize();

    @FXML
    private Pane surface;
    @FXML
    private Canvas canvas;

    private final ObjectProperty<ToolKind> toolKind = new SimpleObjectProperty<>(this, "toolKind", ToolKind.SELECT);
    private final DoubleProperty zoom = new SimpleDoubleProperty(this, "zoom", 1);
    private final InvalidationListener repaintListener = obs -> repaint();
    private final InvalidationListener selectionListener = obs -> onSelectionChanged();
    private ToolSettings settings = new ToolSettings();
    private Tool tool = ToolKind.SELECT.create();
    private ProjectModel model;

    // Text editing
    private TextArea editor;
    private TextLayer editing;
    private boolean editingIsNew;
    private String originalText;

    /** Maps canvas pixels back to image coordinates. */
    private final Scale canvasToImage = new Scale(1, 1, 0, 0);
    /** Canvas pixels per image pixel, on each axis. */
    private double renderScaleX = 1;
    private double renderScaleY = 1;

    @FXML
    private void initialize() {
        surface.scaleXProperty().bind(zoom);
        surface.scaleYProperty().bind(zoom);
        canvas.getTransforms().add(canvasToImage);
        canvas.setFocusTraversable(true);
        zoom.addListener(obs -> updateRenderScale()); // also repaints: overlays keep a constant on-screen width
        // Moving the window to a screen with another scaling changes the resolution needed
        canvas.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.windowProperty().addListener((o, oldWindow, window) -> {
                    if (window != null) {
                        window.outputScaleXProperty().addListener(scale -> updateRenderScale());
                        updateRenderScale();
                    }
                });
            }
        });
        toolKind.addListener((obs, old, kind) -> {
            commitText();
            tool.cancel(this);
            tool = kind.create();
            canvas.setCursor(Cursor.DEFAULT);
            repaint();
        });
        setModel(null);
    }

    public void setSettings(ToolSettings settings) {
        this.settings = settings;
    }

    @Override
    public ToolSettings settings() {
        return settings;
    }

    /** Shows the given project, or an empty canvas when {@code null}. */
    public void setModel(ProjectModel newModel) {
        if (model != null) {
            commitText();
            tool.cancel(this);
            model.getLayers().removeListener(repaintListener);
            model.selectedProperty().removeListener(selectionListener);
            model.revisionProperty().removeListener(repaintListener);
        }
        model = newModel;
        tool = toolKind.get().create();
        if (model == null) {
            canvas.setWidth(0);
            canvas.setHeight(0);
            return;
        }
        updateRenderScale();
        model.getLayers().addListener(repaintListener);
        model.selectedProperty().addListener(selectionListener);
        model.revisionProperty().addListener(repaintListener);
        repaint();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private static int maxCanvasSize() {
        int limit = Integer.getInteger("prism.maxTextureSize", 4096);
        return limit <= 0 ? 8192 : limit;
    }

    /** Resizes the canvas to the resolution it is displayed at, within the texture limit, and repaints. */
    private void updateRenderScale() {
        if (model == null) {
            return;
        }
        double screenScale = canvas.getScene() != null && canvas.getScene().getWindow() != null
                ? canvas.getScene().getWindow().getOutputScaleX()
                : 1;
        double largest = Math.max(1, Math.max(model.getWidth(), model.getHeight()));
        double scale = Math.min(zoom.get() * screenScale, MAX_CANVAS_SIZE / largest);
        double width = Math.max(1, Math.min(MAX_CANVAS_SIZE, Math.ceil(model.getWidth() * scale)));
        double height = Math.max(1, Math.min(MAX_CANVAS_SIZE, Math.ceil(model.getHeight() * scale)));
        canvas.setWidth(width);
        canvas.setHeight(height);
        // Exact per-axis factors, so that the scaled canvas covers the image exactly despite the rounding
        renderScaleX = width / model.getWidth();
        renderScaleY = height / model.getHeight();
        canvasToImage.setX(1 / renderScaleX);
        canvasToImage.setY(1 / renderScaleY);
        repaint();
    }

    private ToolEvent toolEvent(MouseEvent e) {
        return ToolEvent.of(e, renderScaleX, renderScaleY);
    }

    // ---- Tools ------------------------------------------------------------------------------

    public ObjectProperty<ToolKind> toolKindProperty() {
        return toolKind;
    }

    public Tool getTool() {
        return tool;
    }

    /** Aborts what the current tool is doing (Esc) without changing the tool. */
    public void cancelTool() {
        commitText();
        tool.cancel(this);
        repaint();
    }

    private void onSelectionChanged() {
        if (editing != null && model.getSelected() != editing) {
            commitText();
        }
        repaint();
    }

    // ---- Zoom -------------------------------------------------------------------------------

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public void zoomBy(double factor) {
        zoom.set(Math.clamp(zoom.get() * factor, MIN_ZOOM, MAX_ZOOM));
    }

    // ---- Mouse ------------------------------------------------------------------------------

    @FXML
    private void onPressed(MouseEvent e) {
        if (model == null || (e.getButton() != MouseButton.PRIMARY && e.getButton() != MouseButton.SECONDARY)) {
            return;
        }
        if (editing != null) {
            commitText(); // clicking outside the text box ends the typing, like in Paint
            return;
        }
        canvas.requestFocus();
        tool.onPress(this, toolEvent(e));
        updateCursor(e);
    }

    @FXML
    private void onDragged(MouseEvent e) {
        if (model != null && editing == null && (e.isPrimaryButtonDown() || e.isSecondaryButtonDown())) {
            tool.onDrag(this, toolEvent(e));
        }
    }

    @FXML
    private void onReleased(MouseEvent e) {
        if (model != null && editing == null) {
            tool.onRelease(this, toolEvent(e));
            updateCursor(e);
        }
    }

    @FXML
    private void onMoved(MouseEvent e) {
        if (model != null && editing == null) {
            tool.onMove(this, toolEvent(e));
            updateCursor(e);
        }
    }

    @FXML
    private void onExited(MouseEvent e) {
        canvas.setCursor(Cursor.DEFAULT);
    }

    private void updateCursor(MouseEvent e) {
        canvas.setCursor(tool.cursorAt(this, e.getX() / renderScaleX, e.getY() / renderScaleY));
    }

    // ---- Text editing -----------------------------------------------------------------------

    @Override
    public void editText(TextLayer layer, boolean isNew) {
        commitText();
        editing = layer;
        editingIsNew = isNew;
        originalText = layer.getText();
        if (!isNew) {
            model.checkpoint(); // a new text was recorded when it was added
        }
        editor = new TextArea(layer.getText());
        editor.getStyleClass().add("canvas-text-editor");
        editor.setWrapText(false);
        editor.textProperty().addListener((obs, old, text) -> {
            layer.setText(text);
            layoutEditor();
        });
        editor.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
            if (key.getCode() == KeyCode.ESCAPE || (key.getCode() == KeyCode.ENTER && key.isShortcutDown())) {
                commitText();
                key.consume();
            }
        });
        surface.getChildren().add(editor);
        layoutEditor();
        repaint();
        editor.requestFocus();
        editor.end();
    }

    /** @return whether a text is being typed on the canvas */
    public boolean isEditingText() {
        return editing != null;
    }

    /** Keeps the text box aligned with the layer and styled like it. */
    private void layoutEditor() {
        if (editing == null) {
            return;
        }
        editor.setFont(editing.getFont());
        Color color = editing.getStyle().color();
        editor.setStyle("-fx-text-fill: " + toCss(color) + ";");
        double lineHeight = editing.getLineHeight();
        int lines = Math.max(1, editing.getText().split("\n", -1).length);
        // Room for the caret and the next character
        editor.setPrefWidth(Math.max(editing.bounds().w(), lineHeight) + lineHeight);
        editor.setPrefHeight(lines * lineHeight + lineHeight * 0.4);
        editor.setLayoutX(editing.getX());
        editor.setLayoutY(editing.getY());
        editor.setRotate(editing.getRotation());
        editor.setOpacity(Math.max(0.3, editing.getOpacity()));
    }

    private static String toCss(Color c) {
        return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.3f)",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255), c.getOpacity());
    }

    /** Ends text typing: keeps the text, or removes the layer if it was left empty. */
    public void commitText() {
        if (editing == null) {
            return;
        }
        TextLayer layer = editing;
        editing = null;
        surface.getChildren().remove(editor);
        editor = null;
        if (layer.getText().isBlank()) {
            if (editingIsNew) {
                model.cancelAdd(layer);
            } else {
                layer.setText(originalText);
                model.discardCheckpoint();
                model.remove(layer);
            }
        } else if (!editingIsNew && layer.getText().equals(originalText)) {
            model.discardCheckpoint();
            model.refresh();
        } else {
            model.changed();
        }
        canvas.requestFocus();
        repaint();
    }

    // ---- ToolContext ------------------------------------------------------------------------

    @Override
    public ProjectModel model() {
        return model;
    }

    @Override
    public double zoom() {
        return zoom.get();
    }

    @Override
    public void repaint() {
        if (model == null) {
            return;
        }
        layoutEditor();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTransform(new Affine());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        gc.scale(renderScaleX, renderScaleY);
        CanvasRenderer.paintLayers(gc, model.getLayers(), editing);
        Layer selected = model.getSelected();
        if (selected != null && selected != editing) {
            CanvasRenderer.paintSelection(gc, selected, zoom.get());
        }
        if (editing == null) {
            tool.paintOverlay(this, gc);
        }
        gc.restore();
    }

    @Override
    public Color colorAt(double x, double y) {
        if (model == null || x < 0 || y < 0 || x >= model.getWidth() || y >= model.getHeight()) {
            return null;
        }
        // Render just the pixel under the cursor, at full resolution
        return ImageExporter.renderRegion(model, (int) x, (int) y, 1, 1).getPixelReader().getColor(0, 0);
    }
}
