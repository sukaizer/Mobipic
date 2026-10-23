package com.mobipic.controller;

import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.render.CanvasRenderer;
import com.mobipic.tool.SelectTool;
import com.mobipic.tool.Tool;
import com.mobipic.tool.ToolContext;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Hosts the drawing canvas: forwards mouse events to the active tool and repaints on model changes.
 */
public final class CanvasController implements ToolContext {

    public static final double MIN_ZOOM = 0.1;
    public static final double MAX_ZOOM = 8;

    @FXML
    private Canvas canvas;

    private final Tool defaultTool = new SelectTool();
    private final ObjectProperty<Tool> tool = new SimpleObjectProperty<>(this, "tool", defaultTool);
    private final DoubleProperty zoom = new SimpleDoubleProperty(this, "zoom", 1);
    private final InvalidationListener repaintListener = obs -> repaint();
    private ProjectModel model;

    @FXML
    private void initialize() {
        canvas.scaleXProperty().bind(zoom);
        canvas.scaleYProperty().bind(zoom);
        canvas.setFocusTraversable(true);
        zoom.addListener(repaintListener); // overlays keep a constant on-screen width
        setModel(null);
    }

    /** Shows the given project, or an empty canvas when {@code null}. */
    public void setModel(ProjectModel newModel) {
        if (model != null) {
            tool.get().cancel(this);
            model.getLayers().removeListener(repaintListener);
            model.selectedProperty().removeListener(repaintListener);
            model.revisionProperty().removeListener(repaintListener);
        }
        model = newModel;
        tool.set(defaultTool);
        if (model == null) {
            canvas.setWidth(0);
            canvas.setHeight(0);
            return;
        }
        canvas.setWidth(model.getWidth());
        canvas.setHeight(model.getHeight());
        model.getLayers().addListener(repaintListener);
        model.selectedProperty().addListener(repaintListener);
        model.revisionProperty().addListener(repaintListener);
        repaint();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    // ---- Tools ------------------------------------------------------------------------------

    public ReadOnlyObjectProperty<Tool> toolProperty() {
        return tool;
    }

    public void setTool(Tool newTool) {
        if (model == null) {
            return;
        }
        tool.get().cancel(this);
        tool.set(newTool == null ? defaultTool : newTool);
        canvas.setCursor(Cursor.DEFAULT);
        repaint();
    }

    /** Aborts the current tool and goes back to selection. */
    public void resetTool() {
        setTool(defaultTool);
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
        if (model == null || e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        canvas.requestFocus();
        tool.get().onPress(this, e.getX(), e.getY());
        updateCursor(e);
    }

    @FXML
    private void onDragged(MouseEvent e) {
        if (model != null && e.isPrimaryButtonDown()) {
            tool.get().onDrag(this, e.getX(), e.getY());
        }
    }

    @FXML
    private void onReleased(MouseEvent e) {
        if (model != null && e.getButton() == MouseButton.PRIMARY) {
            tool.get().onRelease(this, e.getX(), e.getY());
            updateCursor(e);
        }
    }

    @FXML
    private void onMoved(MouseEvent e) {
        if (model != null) {
            tool.get().onMove(this, e.getX(), e.getY());
            updateCursor(e);
        }
    }

    @FXML
    private void onExited(MouseEvent e) {
        canvas.setCursor(Cursor.DEFAULT);
    }

    private void updateCursor(MouseEvent e) {
        canvas.setCursor(tool.get().cursorAt(this, e.getX(), e.getY()));
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
        CanvasRenderer.paintLayers(canvas, model.getLayers());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Layer selected = model.getSelected();
        if (selected != null) {
            CanvasRenderer.paintSelection(gc, selected, zoom.get());
        }
        tool.get().paintOverlay(this, gc);
    }

    @Override
    public void toolFinished() {
        resetTool();
    }
}
