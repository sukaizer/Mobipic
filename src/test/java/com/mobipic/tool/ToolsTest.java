package com.mobipic.tool;

import com.mobipic.FxToolkit;
import com.mobipic.model.Box;
import com.mobipic.model.EllipseLayer;
import com.mobipic.model.FillMode;
import com.mobipic.model.LineLayer;
import com.mobipic.model.PathLayer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.RectangleLayer;
import com.mobipic.model.TextLayer;
import com.mobipic.model.TriangleLayer;
import com.mobipic.render.CanvasRenderer;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolsTest {

    /** A tool context without a canvas. */
    private static final class TestContext implements ToolContext {
        final ProjectModel model;
        final ToolSettings settings = new ToolSettings();
        TextLayer edited;
        boolean editedIsNew;
        Color color = Color.CORAL;

        TestContext(ProjectModel model) {
            this.model = model;
        }

        @Override
        public ProjectModel model() {
            return model;
        }

        @Override
        public ToolSettings settings() {
            return settings;
        }

        @Override
        public double zoom() {
            return 1;
        }

        @Override
        public void repaint() {
        }

        @Override
        public void editText(TextLayer layer, boolean isNew) {
            edited = layer;
            editedIsNew = isNew;
        }

        @Override
        public Color colorAt(double x, double y) {
            return color;
        }
    }

    private ProjectModel model;
    private TestContext ctx;

    @BeforeAll
    static void startFx() {
        FxToolkit.start();
    }

    @BeforeEach
    void setUp() {
        model = new ProjectModel(FxToolkit.solidImage(400, 300, Color.WHITE));
        ctx = new TestContext(model);
    }

    private static ToolEvent at(double x, double y) {
        return ToolEvent.at(x, y);
    }

    private static ToolEvent shift(double x, double y) {
        return new ToolEvent(x, y, true, false, false, 1);
    }

    private static void drag(Tool tool, ToolContext ctx, ToolEvent from, ToolEvent to) {
        tool.onPress(ctx, from);
        tool.onDrag(ctx, to);
        tool.onRelease(ctx, to);
    }

    @Test
    void shapeToolDrawsInAnyDirectionWithTheCurrentStyle() {
        ctx.settings.primaryProperty().set(Color.RED);
        ctx.settings.secondaryProperty().set(Color.YELLOW);
        ctx.settings.fillModeProperty().set(FillMode.OUTLINE_AND_FILL);
        ctx.settings.lineWidthProperty().set(5);
        ShapeTool tool = new ShapeTool(ShapeTool.Shape.RECTANGLE);
        drag(tool, ctx, at(200, 150), at(100, 100));

        RectangleLayer rect = assertInstanceOf(RectangleLayer.class, model.getSelected());
        assertEquals(new Box(100, 100, 100, 50), rect.bounds());
        assertEquals(Color.RED, rect.getStyle().stroke());
        assertEquals(Color.YELLOW, rect.getStyle().fill());
        assertEquals(5, rect.getStyle().lineWidth());
        assertEquals(FillMode.OUTLINE_AND_FILL, rect.getStyle().fillMode());
    }

    @Test
    void shapeToolStaysActiveForTheNextShape() {
        ShapeTool tool = new ShapeTool(ShapeTool.Shape.ELLIPSE);
        drag(tool, ctx, at(10, 10), at(50, 30));
        drag(tool, ctx, at(100, 100), at(150, 120));
        assertEquals(3, model.getLayers().size());
    }

    @Test
    void shiftDrawsCirclesAndAltDrawsFromTheCentre() {
        ShapeTool ellipse = new ShapeTool(ShapeTool.Shape.ELLIPSE);
        drag(ellipse, ctx, at(0, 0), shift(40, 10));
        EllipseLayer circle = (EllipseLayer) model.getSelected();
        assertEquals(circle.getWidth(), circle.getHeight());
        assertEquals("layer.circle", circle.typeKey());

        ShapeTool rect = new ShapeTool(ShapeTool.Shape.RECTANGLE);
        drag(rect, ctx, at(100, 100), new ToolEvent(120, 110, false, true, false, 1));
        assertEquals(new Box(80, 90, 40, 20), model.getSelected().bounds());
    }

    @Test
    void roundedRectangleUsesTheCornerRadiusSetting() {
        ctx.settings.cornerRadiusProperty().set(15);
        drag(new ShapeTool(ShapeTool.Shape.ROUNDED_RECTANGLE), ctx, at(0, 0), at(100, 60));
        assertEquals(15, ((RectangleLayer) model.getSelected()).getCornerRadius());
        drag(new ShapeTool(ShapeTool.Shape.RECTANGLE), ctx, at(0, 0), at(100, 60));
        assertEquals(0, ((RectangleLayer) model.getSelected()).getCornerRadius());
    }

    @Test
    void aPlainClickDoesNotLeaveAnEmptyShape() {
        ShapeTool tool = new ShapeTool(ShapeTool.Shape.RECTANGLE);
        tool.onPress(ctx, at(10, 10));
        tool.onRelease(ctx, at(10, 10));
        assertEquals(1, model.getLayers().size());
        assertFalse(model.canUndoProperty().get());
    }

    @Test
    void lineSnapsTo45DegreesWithShift() {
        drag(new ShapeTool(ShapeTool.Shape.LINE), ctx, at(0, 0), shift(100, 80));
        LineLayer line = (LineLayer) model.getSelected();
        assertEquals(line.getX2(), line.getY2(), 1e-6);
    }

    @Test
    void triangleCanBeDrawnByDragOrByThreeClicks() {
        ShapeTool tool = new ShapeTool(ShapeTool.Shape.TRIANGLE);
        drag(tool, ctx, at(0, 0), at(100, 100));
        assertEquals(new Box(0, 0, 100, 100), model.getSelected().bounds());

        tool.onPress(ctx, at(200, 200));
        tool.onRelease(ctx, at(200, 200)); // click: vertex 1
        tool.onMove(ctx, at(250, 200));
        tool.onPress(ctx, at(260, 200)); // vertex 2
        tool.onRelease(ctx, at(260, 200));
        tool.onPress(ctx, at(230, 150)); // vertex 3
        tool.onRelease(ctx, at(230, 150));
        TriangleLayer clicked = (TriangleLayer) model.getSelected();
        assertEquals(new Point2D(260, 200), clicked.handles().get(1));
        assertEquals(new Point2D(230, 150), clicked.handles().get(2));
        assertEquals(3, model.getLayers().size());
    }

    @Test
    void cancellingAClickedTriangleRemovesIt() {
        ShapeTool tool = new ShapeTool(ShapeTool.Shape.TRIANGLE);
        tool.onPress(ctx, at(10, 10));
        tool.onRelease(ctx, at(10, 10));
        tool.cancel(ctx);
        assertEquals(1, model.getLayers().size());
    }

    @Test
    void brushRecordsAStrokeAsOneUndoStep() {
        ctx.settings.primaryProperty().set(Color.GREEN);
        BrushTool brush = new BrushTool();
        brush.onPress(ctx, at(10, 10));
        for (int i = 1; i <= 20; i++) {
            brush.onDrag(ctx, at(10 + i * 5, 10 + i));
        }
        brush.onRelease(ctx, at(120, 31));
        PathLayer path = assertInstanceOf(PathLayer.class, model.getSelected());
        assertTrue(path.pointCount() >= 20);
        assertEquals(Color.GREEN, path.getStyle().stroke());
        model.undo();
        assertEquals(1, model.getLayers().size());
    }

    @Test
    void selectToolMovesTheLayerUnderTheCursor() {
        RectangleLayer rect = new RectangleLayer(50, 50, 100, 100, ctx.settings.shapeStyle());
        model.add(rect);
        model.select(null);
        SelectTool tool = new SelectTool();
        drag(tool, ctx, at(100, 100), at(130, 90));
        assertSame(rect, model.getSelected());
        assertEquals(new Box(80, 40, 100, 100), rect.bounds());
        model.undo();
        assertEquals(50, model.getLayers().get(1).getX());
    }

    @Test
    void selectToolResizesWithHandlesAndRotatesWithTheKnob() {
        RectangleLayer rect = new RectangleLayer(100, 100, 100, 50, ctx.settings.shapeStyle());
        model.add(rect);
        SelectTool tool = new SelectTool();

        drag(tool, ctx, at(200, 150), at(250, 200)); // bottom-right handle
        assertEquals(new Box(100, 100, 150, 100), rect.bounds());

        Point2D knob = CanvasRenderer.rotationHandle(rect, 1);
        Point2D c = rect.center();
        // Drag the knob from above the centre to its right: a quarter turn clockwise
        drag(tool, ctx, at(knob.getX(), knob.getY()), new ToolEvent(c.getX() + 100, c.getY(), true, false, false, 1));
        assertEquals(90, rect.getRotation(), 1e-6);
    }

    @Test
    void clickingOutsideLayersSelectsTheBaseOrNothing() {
        model.add(new RectangleLayer(0, 0, 10, 10, ctx.settings.shapeStyle()));
        SelectTool tool = new SelectTool();
        tool.onPress(ctx, at(300, 200));
        assertSame(model.getBase(), model.getSelected());
        tool.onPress(ctx, at(1000, 1000));
        assertNull(model.getSelected());
    }

    @Test
    void doubleClickingATextEditsIt() {
        TextLayer text = new TextLayer(50, 50, "Bonjour", ctx.settings.textStyle());
        model.add(text);
        new SelectTool().onPress(ctx, new ToolEvent(55, 60, false, false, false, 2));
        assertSame(text, ctx.edited);
        assertFalse(ctx.editedIsNew);
    }

    @Test
    void textToolCreatesATextWhereClicked() {
        ctx.settings.fontSizeProperty().set(48);
        new TextTool().onPress(ctx, at(120, 80));
        TextLayer text = assertInstanceOf(TextLayer.class, ctx.edited);
        assertTrue(ctx.editedIsNew);
        assertEquals(120, text.getX());
        assertEquals(80, text.getY());
        assertEquals(48, text.getStyle().size());
        assertSame(text, model.getSelected());
    }

    @Test
    void eyedropperSetsThePrimaryOrSecondaryColour() {
        EyedropperTool tool = new EyedropperTool();
        tool.onPress(ctx, at(5, 5));
        assertEquals(Color.CORAL, ctx.settings.primaryProperty().get());
        ctx.color = Color.NAVY;
        tool.onPress(ctx, new ToolEvent(5, 5, false, false, true, 1));
        assertEquals(Color.NAVY, ctx.settings.secondaryProperty().get());
        assertEquals(Color.CORAL, ctx.settings.primaryProperty().get());
    }

    @Test
    void everyToolKindHasAShortcutAndCreatesItsTool() {
        for (ToolKind kind : ToolKind.values()) {
            assertSame(kind, ToolKind.forShortcut(kind.shortcut()));
            Tool tool = kind.create();
            assertTrue(tool.hintKey().startsWith("tool."));
        }
    }
}
