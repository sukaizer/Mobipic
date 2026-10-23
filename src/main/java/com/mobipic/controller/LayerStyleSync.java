package com.mobipic.controller;

import com.mobipic.model.ImageLayer;
import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.ShapeLayer;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TextStyle;
import com.mobipic.tool.ToolSettings;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;

import java.util.List;
import java.util.function.Predicate;

/**
 * Keeps the {@link ToolSettings} and the selected layer in sync, like the options bar of Photoshop:
 * selecting a layer shows its style in the options; changing an option restyles the selected layer.
 * <p>
 * Successive changes of the same option on the same layer (dragging a slider, spinning a value) are
 * grouped into a single undo step.
 */
public final class LayerStyleSync {

    /** Changes of one option closer than this are merged into one undo step. */
    private static final long MERGE_DELAY_MS = 1000;

    private final ToolSettings settings;
    private final InvalidationListener selectionListener = obs -> loadFromSelection();
    private ProjectModel model;
    private boolean loading;

    private Layer lastLayer;
    private Object lastOption;
    private long lastChange;

    public LayerStyleSync(ToolSettings settings) {
        this.settings = settings;
        watch(settings.primaryProperty(), this::applyPrimary);
        watch(settings.secondaryProperty(), this::applySecondary);
        watch(settings.lineWidthProperty(), layer -> layer instanceof ShapeLayer s
                && restyle(s, s.getStyle().withLineWidth(Math.max(0.5, settings.lineWidthProperty().get()))));
        watch(settings.fillModeProperty(), layer -> layer instanceof ShapeLayer s && s.canFill()
                && restyle(s, s.getStyle().withFillMode(settings.fillModeProperty().get())));
        watch(settings.cornerRadiusProperty(), this::applyCornerRadius);
        watch(settings.opacityProperty(), this::applyOpacity);
        for (ObservableValue<?> textOption : List.of(settings.fontFamilyProperty(), settings.fontSizeProperty(),
                settings.boldProperty(), settings.italicProperty(), settings.alignmentProperty(),
                settings.textOutlineProperty(), settings.shadowProperty())) {
            watch(textOption, this::applyTextStyle);
        }
    }

    public void setModel(ProjectModel newModel) {
        if (model != null) {
            model.selectedProperty().removeListener(selectionListener);
        }
        model = newModel;
        lastLayer = null;
        if (model != null) {
            model.selectedProperty().addListener(selectionListener);
            loadFromSelection();
        }
    }

    /**
     * @param apply restyles the layer and returns whether something changed
     */
    private void watch(ObservableValue<?> option, Predicate<Layer> apply) {
        // A change listener (not an invalidation listener) so that every change is seen, read or not
        option.addListener((obs, old, value) -> {
            if (loading || model == null) {
                return;
            }
            Layer layer = model.getSelected();
            if (!model.isEditable(layer)) {
                return;
            }
            // Take the undo snapshot before changing anything, merging rapid changes of the same option
            long now = System.currentTimeMillis();
            boolean merge = layer == lastLayer && option == lastOption && now - lastChange < MERGE_DELAY_MS;
            if (!merge) {
                model.checkpoint();
            }
            if (apply.test(layer)) {
                lastLayer = layer;
                lastOption = option;
                lastChange = now;
                model.changed();
            } else if (!merge) {
                model.discardCheckpoint(); // nothing changed: no undo step
            }
        });
    }

    // ---- Settings -> layer --------------------------------------------------------------------

    private boolean applyPrimary(Layer layer) {
        return switch (layer) {
            case ShapeLayer s -> restyle(s, s.getStyle().withStroke(settings.primaryProperty().get()));
            case TextLayer t -> retext(t, t.getStyle().withColor(settings.primaryProperty().get()));
            default -> false;
        };
    }

    private boolean applySecondary(Layer layer) {
        return switch (layer) {
            case ShapeLayer s when s.canFill() -> restyle(s, s.getStyle().withFill(settings.secondaryProperty().get()));
            case TextLayer t -> retext(t, t.getStyle().withOutlineColor(settings.secondaryProperty().get()));
            default -> false;
        };
    }

    private boolean applyCornerRadius(Layer layer) {
        if (layer instanceof RectangleLayer r && r.getCornerRadius() != settings.cornerRadiusProperty().get()) {
            r.setCornerRadius(settings.cornerRadiusProperty().get());
            return true;
        }
        return false;
    }

    private boolean applyOpacity(Layer layer) {
        double opacity = settings.opacityProperty().get();
        if (layer.getOpacity() == opacity) {
            return false;
        }
        layer.setOpacity(opacity);
        return true;
    }

    private boolean applyTextStyle(Layer layer) {
        if (layer instanceof TextLayer t) {
            // Keep the text's own colours: only font options changed
            TextStyle s = settings.textStyle();
            return retext(t, new TextStyle(s.family(), s.size(), s.bold(), s.italic(), s.alignment(),
                    t.getStyle().color(), t.getStyle().outlineColor(), s.outlineWidth(), s.shadow()));
        }
        return false;
    }

    private static boolean restyle(ShapeLayer layer, ShapeStyle style) {
        if (style.equals(layer.getStyle())) {
            return false;
        }
        layer.setStyle(style);
        return true;
    }

    private static boolean retext(TextLayer layer, TextStyle style) {
        if (style.equals(layer.getStyle())) {
            return false;
        }
        layer.setStyle(style);
        return true;
    }

    // ---- Layer -> settings --------------------------------------------------------------------

    private void loadFromSelection() {
        if (model == null) {
            return;
        }
        Layer layer = model.getSelected();
        if (!model.isEditable(layer)) {
            return;
        }
        loading = true;
        try {
            switch (layer) {
                case ShapeLayer s -> {
                    ShapeStyle style = s.getStyle();
                    settings.primaryProperty().set(style.stroke());
                    settings.lineWidthProperty().set(style.lineWidth());
                    if (s.canFill()) {
                        settings.secondaryProperty().set(style.fill());
                        settings.fillModeProperty().set(style.fillMode());
                    }
                    if (s instanceof RectangleLayer r && r.getCornerRadius() > 0) {
                        settings.cornerRadiusProperty().set(r.getCornerRadius());
                    }
                }
                case TextLayer t -> {
                    TextStyle style = t.getStyle();
                    settings.primaryProperty().set(style.color());
                    settings.secondaryProperty().set(style.outlineColor());
                    settings.fontFamilyProperty().set(style.family());
                    settings.fontSizeProperty().set(style.size());
                    settings.boldProperty().set(style.bold());
                    settings.italicProperty().set(style.italic());
                    settings.alignmentProperty().set(style.alignment());
                    settings.textOutlineProperty().set(style.outlineWidth());
                    settings.shadowProperty().set(style.shadow());
                }
                case ImageLayer i -> {
                    // only the opacity below
                }
            }
            settings.opacityProperty().set(layer.getOpacity());
        } finally {
            loading = false;
        }
    }
}
