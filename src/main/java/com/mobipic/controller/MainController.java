package com.mobipic.controller;

import com.mobipic.io.ImageExporter;
import com.mobipic.io.ProjectIO;
import com.mobipic.model.ImageLayer;
import com.mobipic.model.Layer;
import com.mobipic.model.ProjectModel;
import com.mobipic.model.ShapeStyle;
import com.mobipic.model.ShapeType;
import com.mobipic.tool.MoveTool;
import com.mobipic.tool.ResizeTool;
import com.mobipic.util.Dialogs;
import com.mobipic.util.I18n;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Main window: menus, toolbar, canvas and layer panel.
 */
public final class MainController {

    private static final String GITHUB_URL = "https://github.com/sukaizer/Mobipic";
    private static final double ZOOM_STEP = 1.25;
    private static final List<String> IMAGE_EXTENSIONS = List.of("*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif");

    // Menus
    @FXML
    private MenuItem saveMenuItem;
    @FXML
    private MenuItem saveAsMenuItem;
    @FXML
    private Menu exportMenu;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private Menu layersMenu;
    @FXML
    private MenuItem addFilterMenu;
    @FXML
    private MenuItem deleteMenuItem;
    @FXML
    private MenuItem moveMenuItem;
    @FXML
    private MenuItem resizeMenuItem;
    @FXML
    private MenuItem duplicateMenuItem;
    @FXML
    private Menu viewMenu;
    @FXML
    private MenuItem zoomInMenuItem;
    @FXML
    private MenuItem zoomOutMenuItem;
    @FXML
    private MenuItem zoomResetMenuItem;

    // Toolbar
    @FXML
    private Button saveButton;
    @FXML
    private Button newShapeButton;
    @FXML
    private Button newImageButton;
    @FXML
    private Button newTextButton;
    @FXML
    private Button zoomPlus;
    @FXML
    private Button zoomMinus;
    @FXML
    private Button zoomReset;
    @FXML
    private Label zoomLabel;
    @FXML
    private Label hintLabel;

    // Canvas area
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane canvasHolder;
    @FXML
    private Label placeholder;
    @FXML
    private CanvasController canvasViewController;

    // Layer panel
    @FXML
    private LayersController layersViewController;
    @FXML
    private Button up;
    @FXML
    private Button down;
    @FXML
    private Button deleteButton;
    @FXML
    private Button moveButton;
    @FXML
    private Button resizeButton;
    @FXML
    private Button duplicateButton;

    private final ObjectProperty<ProjectModel> project = new SimpleObjectProperty<>(this, "project");
    private final BooleanProperty projectOpen = new SimpleBooleanProperty();
    private final BooleanProperty layerEditable = new SimpleBooleanProperty();
    private final BooleanProperty layerResizable = new SimpleBooleanProperty();
    private final BooleanProperty imageSelected = new SimpleBooleanProperty();
    private final BooleanProperty canSendBackward = new SimpleBooleanProperty();
    private final BooleanProperty canBringForward = new SimpleBooleanProperty();
    private final BooleanProperty canUndo = new SimpleBooleanProperty();
    private final BooleanProperty canRedo = new SimpleBooleanProperty();
    private final InvalidationListener stateListener = obs -> refreshState();
    private final InvalidationListener titleListener = obs -> updateTitle();

    private Stage stage;
    private HostServices hostServices;
    private ShapeType lastShapeType;
    private ShapeStyle lastShapeStyle = ShapeStyle.DEFAULT;
    private File lastDirectory;

    @FXML
    private void initialize() {
        // Character-based accelerators work whatever the keyboard layout (AZERTY, QWERTY...)
        zoomInMenuItem.setAccelerator(new KeyCharacterCombination("+", KeyCombination.SHORTCUT_DOWN));
        zoomOutMenuItem.setAccelerator(new KeyCharacterCombination("-", KeyCombination.SHORTCUT_DOWN));
        zoomResetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN));

        bindControls();
        wireLayerContextMenu();

        placeholder.visibleProperty().bind(projectOpen.not());
        placeholder.managedProperty().bind(projectOpen.not());

        zoomLabel.textProperty().bind(Bindings.format(Locale.ROOT, "%.0f %%",
                canvasViewController.zoomProperty().multiply(100)));
        hintLabel.textProperty().bind(Bindings.createStringBinding(
                () -> projectOpen.get() ? I18n.get(canvasViewController.toolProperty().get().hintKey()) : "",
                canvasViewController.toolProperty(), projectOpen));

        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::onScroll);
        installDragAndDrop();

        project.addListener((obs, old, model) -> projectChanged(old, model));
        refreshState();
    }

    /** Called once the window exists. */
    public void init(Stage stage, HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
        stage.setOnCloseRequest(event -> {
            event.consume();
            quitApp();
        });
        Scene scene = stage.getScene();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onArrowKey);
        updateTitle();
    }

    // ---- State ------------------------------------------------------------------------------

    private void bindControls() {
        for (var control : List.of(saveButton, newShapeButton, newImageButton, newTextButton,
                zoomPlus, zoomMinus, zoomReset)) {
            control.disableProperty().bind(projectOpen.not());
        }
        for (var item : List.of(saveMenuItem, saveAsMenuItem, exportMenu, layersMenu, viewMenu)) {
            item.disableProperty().bind(projectOpen.not());
        }
        for (var control : List.of(deleteButton, moveButton, duplicateButton)) {
            control.disableProperty().bind(layerEditable.not());
        }
        for (var item : List.of(deleteMenuItem, moveMenuItem, duplicateMenuItem)) {
            item.disableProperty().bind(layerEditable.not());
        }
        resizeButton.disableProperty().bind(layerResizable.not());
        resizeMenuItem.disableProperty().bind(layerResizable.not());
        addFilterMenu.disableProperty().bind(imageSelected.not());
        up.disableProperty().bind(canSendBackward.not());
        down.disableProperty().bind(canBringForward.not());
        undoMenuItem.disableProperty().bind(canUndo.not());
        redoMenuItem.disableProperty().bind(canRedo.not());
    }

    private void wireLayerContextMenu() {
        LayersController layers = layersViewController;
        layers.delete.setOnAction(e -> deleteLayer());
        layers.move.setOnAction(e -> moveLayer());
        layers.resize.setOnAction(e -> resizeLayer());
        layers.duplicate.setOnAction(e -> duplicateLayer());
        layers.filters.setOnAction(e -> addFilter());
        layers.delete.disableProperty().bind(layerEditable.not());
        layers.move.disableProperty().bind(layerEditable.not());
        layers.duplicate.disableProperty().bind(layerEditable.not());
        layers.resize.disableProperty().bind(layerResizable.not());
        layers.filters.disableProperty().bind(imageSelected.not());
    }

    private void projectChanged(ProjectModel old, ProjectModel model) {
        if (old != null) {
            old.getLayers().removeListener(stateListener);
            old.selectedProperty().removeListener(stateListener);
            old.canUndoProperty().removeListener(stateListener);
            old.canRedoProperty().removeListener(stateListener);
            old.dirtyProperty().removeListener(titleListener);
            old.fileProperty().removeListener(titleListener);
        }
        if (model != null) {
            model.getLayers().addListener(stateListener);
            model.selectedProperty().addListener(stateListener);
            model.canUndoProperty().addListener(stateListener);
            model.canRedoProperty().addListener(stateListener);
            model.dirtyProperty().addListener(titleListener);
            model.fileProperty().addListener(titleListener);
        }
        // Reuse the same canvas and layer list instead of stacking new ones
        canvasViewController.setModel(model);
        layersViewController.setModel(model);
        canvasViewController.zoomProperty().set(1);
        refreshState();
        updateTitle();
    }

    private void refreshState() {
        ProjectModel model = project.get();
        Layer selected = model == null ? null : model.getSelected();
        projectOpen.set(model != null);
        layerEditable.set(model != null && model.isEditable(selected));
        layerResizable.set(layerEditable.get() && selected.isResizable());
        imageSelected.set(selected instanceof ImageLayer);
        canSendBackward.set(model != null && model.canSendBackward(selected));
        canBringForward.set(model != null && model.canBringForward(selected));
        canUndo.set(model != null && model.canUndoProperty().get());
        canRedo.set(model != null && model.canRedoProperty().get());
    }

    private void updateTitle() {
        if (stage == null) {
            return;
        }
        ProjectModel model = project.get();
        if (model == null) {
            stage.setTitle("Mobipic");
            return;
        }
        String name = model.getFile() == null ? I18n.get("title.untitled") : model.getFile().getFileName().toString();
        stage.setTitle((model.isDirty() ? "* " : "") + name + " - Mobipic");
    }

    private ProjectModel model() {
        return project.get();
    }

    // ---- File menu --------------------------------------------------------------------------

    @FXML
    private void newProject() {
        if (!confirmDiscard()) {
            return;
        }
        File file = imageChooser("chooser.newProject").showOpenDialog(stage);
        if (file != null) {
            openImageAsProject(file.toPath());
        }
    }

    @FXML
    private void openProject() {
        if (!confirmDiscard()) {
            return;
        }
        File file = projectChooser("chooser.openProject").showOpenDialog(stage);
        if (file != null) {
            openProjectFile(file.toPath());
        }
    }

    /** Opens a project or an image, depending on the extension. Used for drag and drop and the command line. */
    public void openFile(Path path) {
        if (!confirmDiscard()) {
            return;
        }
        if (isProjectFile(path)) {
            openProjectFile(path);
        } else {
            openImageAsProject(path);
        }
    }

    private void openImageAsProject(Path path) {
        Image image = loadImage(path);
        if (image != null) {
            rememberDirectory(path);
            project.set(new ProjectModel(image));
        }
    }

    private void openProjectFile(Path path) {
        try {
            ProjectModel model = ProjectIO.load(path);
            model.markSaved(path);
            rememberDirectory(path);
            project.set(model);
        } catch (IOException | RuntimeException e) {
            Dialogs.error(stage, "error.open", e);
        }
    }

    @FXML
    private void saveProject() {
        save();
    }

    @FXML
    private void saveProjectAs() {
        saveAs();
    }

    /** @return whether the project was saved */
    private boolean save() {
        if (model() == null) {
            return false;
        }
        Path file = model().getFile();
        return file == null ? saveAs() : saveTo(file);
    }

    private boolean saveAs() {
        if (model() == null) {
            return false;
        }
        FileChooser chooser = projectChooser("chooser.saveProject");
        chooser.setInitialFileName(model().getFile() == null
                ? I18n.get("title.untitled") + "." + ProjectIO.EXTENSION
                : model().getFile().getFileName().toString());
        File file = chooser.showSaveDialog(stage);
        return file != null && saveTo(withExtension(file.toPath(), ProjectIO.EXTENSION));
    }

    private boolean saveTo(Path file) {
        try {
            ProjectIO.save(model(), file);
            model().markSaved(file);
            rememberDirectory(file);
            return true;
        } catch (IOException | RuntimeException e) {
            Dialogs.error(stage, "error.save", e);
            return false;
        }
    }

    @FXML
    private void exportToJpg() {
        export(ImageExporter.Format.JPG);
    }

    @FXML
    private void exportToBmp() {
        export(ImageExporter.Format.BMP);
    }

    @FXML
    private void exportToPng() {
        export(ImageExporter.Format.PNG);
    }

    private void export(ImageExporter.Format format) {
        if (model() == null) {
            return;
        }
        FileChooser chooser = chooser("chooser.export");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                I18n.format("filter.format", format.name()), "*." + format.extension()));
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        Path target = file.toPath();
        if (ImageExporter.formatOf(target) != format) {
            target = withExtension(target, format.extension());
        }
        try {
            ImageExporter.export(model(), format, target);
            rememberDirectory(target);
        } catch (IOException | RuntimeException e) {
            Dialogs.error(stage, "error.export", e);
        }
    }

    @FXML
    private void quitApp() {
        if (confirmDiscard()) {
            Platform.exit();
        }
    }

    /** @return {@code true} if there is nothing to lose, or the user chose to save or discard */
    private boolean confirmDiscard() {
        ProjectModel model = model();
        if (model == null || !model.isDirty()) {
            return true;
        }
        ConfirmDialogController dialog = Dialogs.showModal(stage, "confirm.fxml", "confirm.title",
                (ConfirmDialogController c) -> c.init(I18n.get("confirm.unsaved")));
        return switch (dialog.getChoice()) {
            case SAVE -> save();
            case DISCARD -> true;
            case CANCEL -> false;
        };
    }

    // ---- Edit menu --------------------------------------------------------------------------

    @FXML
    private void undo() {
        if (model() != null) {
            canvasViewController.resetTool();
            model().undo();
        }
    }

    @FXML
    private void redo() {
        if (model() != null) {
            canvasViewController.resetTool();
            model().redo();
        }
    }

    // ---- Layer menu -------------------------------------------------------------------------

    @FXML
    private void addShapeMenu() {
        if (model() == null) {
            return;
        }
        ShapeDialogController dialog = Dialogs.showModal(stage, "shapes.fxml", "shapes.title",
                (ShapeDialogController c) -> c.init(lastShapeType, lastShapeStyle));
        dialog.getResult().ifPresent(tool -> {
            lastShapeType = dialog.getSelectedType();
            lastShapeStyle = dialog.getStyle();
            canvasViewController.setTool(tool);
        });
    }

    @FXML
    private void addImageLayer() {
        if (model() == null) {
            return;
        }
        File file = imageChooser("chooser.addImage").showOpenDialog(stage);
        if (file != null) {
            addImage(file.toPath());
        }
    }

    private void addImage(Path path) {
        Image image = loadImage(path);
        if (image != null) {
            rememberDirectory(path);
            model().add(new ImageLayer(image, 0, 0));
        }
    }

    @FXML
    private void addTextMenu() {
        ProjectModel model = model();
        if (model == null) {
            return;
        }
        TextDialogController dialog = Dialogs.showModal(stage, "text.fxml", "text.title",
                (TextDialogController c) -> c.init(model.getWidth() / 2, model.getHeight() / 2));
        dialog.getResult().ifPresent(model::add);
    }

    @FXML
    private void addFilter() {
        ProjectModel model = model();
        if (model != null && model.getSelected() instanceof ImageLayer image) {
            Dialogs.showModal(stage, "filters.fxml", "filters.title",
                    (FilterDialogController c) -> c.init(model, image));
        }
    }

    @FXML
    private void deleteLayer() {
        if (layerEditable.get()) {
            canvasViewController.resetTool();
            model().remove(model().getSelected());
        }
    }

    @FXML
    private void moveLayer() {
        if (layerEditable.get()) {
            canvasViewController.setTool(new MoveTool());
        }
    }

    @FXML
    private void resizeLayer() {
        if (layerResizable.get()) {
            canvasViewController.setTool(new ResizeTool());
        }
    }

    @FXML
    private void duplicateLayer() {
        if (layerEditable.get()) {
            model().duplicate(model().getSelected());
        }
    }

    /** "Up" arrow: moves the layer up in the list, i.e. further back in the drawing order. */
    @FXML
    private void sendBack() {
        if (canSendBackward.get()) {
            model().sendBackward(model().getSelected());
        }
    }

    /** "Down" arrow: moves the layer down in the list, i.e. closer to the front. */
    @FXML
    private void sendForward() {
        if (canBringForward.get()) {
            model().bringForward(model().getSelected());
        }
    }

    // ---- View menu --------------------------------------------------------------------------

    @FXML
    private void zoomPlus() {
        canvasViewController.zoomBy(ZOOM_STEP);
    }

    @FXML
    private void zoomMinus() {
        canvasViewController.zoomBy(1 / ZOOM_STEP);
    }

    @FXML
    private void zoomReset() {
        canvasViewController.zoomProperty().set(1);
    }

    private void onScroll(ScrollEvent event) {
        if (event.isShortcutDown() && model() != null && event.getDeltaY() != 0) {
            canvasViewController.zoomBy(event.getDeltaY() > 0 ? ZOOM_STEP : 1 / ZOOM_STEP);
            event.consume();
        }
    }

    // ---- Help menu --------------------------------------------------------------------------

    @FXML
    private void helpButtonAction() {
        hostServices.showDocument(GITHUB_URL);
    }

    // ---- Keyboard ---------------------------------------------------------------------------

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && model() != null) {
            canvasViewController.resetTool();
            model().select(null);
            event.consume();
        } else if (event.getCode() == KeyCode.DELETE && layerEditable.get()) {
            deleteLayer();
            event.consume();
        }
    }

    /** Arrow keys nudge the selected layer (1 px, 10 px with Shift) when the canvas has the focus. */
    private void onArrowKey(KeyEvent event) {
        if (!event.getCode().isArrowKey() || !layerEditable.get()
                || stage.getScene().getFocusOwner() != canvasViewController.getCanvas()) {
            return;
        }
        double step = event.isShiftDown() ? 10 : 1;
        double dx = switch (event.getCode()) {
            case LEFT -> -step;
            case RIGHT -> step;
            default -> 0;
        };
        double dy = switch (event.getCode()) {
            case UP -> -step;
            case DOWN -> step;
            default -> 0;
        };
        model().checkpoint();
        model().getSelected().translate(dx, dy);
        model().changed();
        event.consume();
    }

    // ---- Drag and drop ----------------------------------------------------------------------

    /** Dropping a project opens it; dropping an image adds it as a layer (or starts a project). */
    private void installDragAndDrop() {
        scrollPane.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        scrollPane.setOnDragDropped(event -> {
            List<File> files = event.getDragboard().getFiles();
            boolean done = !files.isEmpty();
            if (done) {
                Path path = files.getFirst().toPath();
                if (isProjectFile(path) || model() == null) {
                    // Let the drop finish before showing any dialog
                    Platform.runLater(() -> openFile(path));
                } else {
                    Platform.runLater(() -> addImage(path));
                }
            }
            event.setDropCompleted(done);
            event.consume();
        });
    }

    // ---- Helpers ----------------------------------------------------------------------------

    private Image loadImage(Path path) {
        Image image = new Image(path.toUri().toString());
        if (image.isError()) {
            Dialogs.error(stage, "error.image", image.getException() != null
                    ? image.getException()
                    : new IOException(path.toString()));
            return null;
        }
        return image;
    }

    private FileChooser chooser(String titleKey) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get(titleKey));
        if (lastDirectory != null && lastDirectory.isDirectory()) {
            chooser.setInitialDirectory(lastDirectory);
        }
        return chooser;
    }

    private FileChooser imageChooser(String titleKey) {
        FileChooser chooser = chooser(titleKey);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.get("filter.images"), IMAGE_EXTENSIONS));
        return chooser;
    }

    private FileChooser projectChooser(String titleKey) {
        FileChooser chooser = chooser(titleKey);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                I18n.get("filter.projects"), "*." + ProjectIO.EXTENSION));
        return chooser;
    }

    private void rememberDirectory(Path file) {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            lastDirectory = parent.toFile();
        }
    }

    private static boolean isProjectFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("." + ProjectIO.EXTENSION);
    }

    private static Path withExtension(Path path, String extension) {
        String name = path.getFileName().toString();
        return name.toLowerCase(Locale.ROOT).endsWith("." + extension) ? path : path.resolveSibling(name + "." + extension);
    }
}
