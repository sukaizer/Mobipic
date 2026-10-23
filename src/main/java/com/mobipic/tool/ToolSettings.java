package com.mobipic.tool;

import com.mobipic.model.FillMode;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.TextStyle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Current drawing options, shared by the tool palette, the options bar and the tools.
 * <p>
 * Like in Paint, there are two colours: the <em>primary</em> colour is used for outlines, brush strokes
 * and text; the <em>secondary</em> colour for fills and text outlines.
 */
public final class ToolSettings {

    private final ObjectProperty<Color> primary = new SimpleObjectProperty<>(this, "primary", Color.BLACK);
    private final ObjectProperty<Color> secondary = new SimpleObjectProperty<>(this, "secondary", Color.WHITE);
    private final DoubleProperty lineWidth = new SimpleDoubleProperty(this, "lineWidth", 3);
    private final ObjectProperty<FillMode> fillMode = new SimpleObjectProperty<>(this, "fillMode", FillMode.OUTLINE);
    private final DoubleProperty cornerRadius = new SimpleDoubleProperty(this, "cornerRadius", 20);
    private final DoubleProperty opacity = new SimpleDoubleProperty(this, "opacity", 1);

    private final StringProperty fontFamily = new SimpleStringProperty(this, "fontFamily", defaultFamily());
    private final DoubleProperty fontSize = new SimpleDoubleProperty(this, "fontSize", 36);
    private final BooleanProperty bold = new SimpleBooleanProperty(this, "bold");
    private final BooleanProperty italic = new SimpleBooleanProperty(this, "italic");
    private final ObjectProperty<TextAlignment> alignment = new SimpleObjectProperty<>(this, "alignment", TextAlignment.LEFT);
    private final DoubleProperty textOutline = new SimpleDoubleProperty(this, "textOutline", 0);
    private final BooleanProperty shadow = new SimpleBooleanProperty(this, "shadow");

    private static String defaultFamily() {
        return Font.getFamilies().contains("Verdana") ? "Verdana" : Font.getDefault().getFamily();
    }

    public ObjectProperty<Color> primaryProperty() {
        return primary;
    }

    public ObjectProperty<Color> secondaryProperty() {
        return secondary;
    }

    public DoubleProperty lineWidthProperty() {
        return lineWidth;
    }

    public ObjectProperty<FillMode> fillModeProperty() {
        return fillMode;
    }

    public DoubleProperty cornerRadiusProperty() {
        return cornerRadius;
    }

    public DoubleProperty opacityProperty() {
        return opacity;
    }

    public StringProperty fontFamilyProperty() {
        return fontFamily;
    }

    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    public BooleanProperty boldProperty() {
        return bold;
    }

    public BooleanProperty italicProperty() {
        return italic;
    }

    public ObjectProperty<TextAlignment> alignmentProperty() {
        return alignment;
    }

    public DoubleProperty textOutlineProperty() {
        return textOutline;
    }

    public BooleanProperty shadowProperty() {
        return shadow;
    }

    /** Swaps the primary and secondary colours (X key, swap button). */
    public void swapColors() {
        Color previous = primary.get();
        primary.set(secondary.get());
        secondary.set(previous);
    }

    public ShapeStyle shapeStyle() {
        return new ShapeStyle(primary.get(), secondary.get(), Math.max(0.5, lineWidth.get()), fillMode.get());
    }

    public TextStyle textStyle() {
        return new TextStyle(fontFamily.get(), Math.max(1, fontSize.get()), bold.get(), italic.get(), alignment.get(),
                primary.get(), secondary.get(), textOutline.get(), shadow.get());
    }
}
