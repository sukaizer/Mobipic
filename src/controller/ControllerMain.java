package controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Rectangle;
import model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControllerMain implements Initializable {

    
    private Stage primaryStage;
    private FileChooser fileChooser;
    private File file;
    private ProjectModel model;
    private ControllerCanvas controllerCanvas;
    private Canvas canvas;
    private ControllerLayers controllerLayers;

    final KeyCombination keyCombinationControlN = new KeyCodeCombination(
            KeyCode.N, KeyCombination.CONTROL_DOWN);

    final KeyCombination keyCombinationControlSuppr = new KeyCodeCombination(
            KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN);

    final KeyCombination keyCombinationControlZ = new KeyCodeCombination(
            KeyCode.Z, KeyCombination.CONTROL_DOWN);

    final KeyCombination keyCombinationControlY = new KeyCodeCombination(
            KeyCode.Y, KeyCombination.CONTROL_DOWN);


    @FXML
    public Menu exportButton;
    @FXML
    public AnchorPane mainPane;
    @FXML
    public Button newShapeButton;
    @FXML
    public AnchorPane layersPane;
    @FXML
    public Button newImageButton;
    @FXML
    public Menu layersMenuBar;
    @FXML
    public VBox layersModifyBar;
    @FXML
    public Button zoomPlus;
    @FXML
    public Button zoomMinus;
    @FXML
    public HBox hboxLayersModify;
    @FXML
    public Button up;
    @FXML
    public Button down;
    @FXML
    public Button newTextButton;
    @FXML
    public MenuItem addFilterMenu;
    @FXML
    public Button deleteButton;
    @FXML
    public Button moveButton;
    @FXML
    public Button resizeButton;
    @FXML
    public Button duplicateButton;
    @FXML
    public MenuItem deleteMenuItem;
    @FXML
    public MenuItem moveMenuItem;
    @FXML
    public MenuItem resizeMenuItem;
    @FXML
    public MenuItem duplicateMenuItem;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDisableItems(true);
        this.newTextButton.setDisable(true);
        this.hboxLayersModify.setDisable(true);
        this.zoomMinus.setDisable(true);
        this.zoomPlus.setDisable(true);
        this.layersModifyBar.setDisable(true);
        this.layersMenuBar.setDisable(true);
        this.newShapeButton.setDisable(true);
        this.newImageButton.setDisable(true);
        this.exportButton.setDisable(true);
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Project Files", "*.mbpc"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.bmp"));
    }

    public void setDisableItems(boolean b) {
        this.deleteMenuItem.setDisable(b);
        this.moveMenuItem.setDisable(b);
        this.resizeMenuItem.setDisable(b);
        this.duplicateMenuItem.setDisable(b);

        this.deleteButton.setDisable(b);
        this.moveButton.setDisable(b);
        this.resizeButton.setDisable(b);
        this.duplicateButton.setDisable(b);
    }

    public void init(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    /**
     * Ouvre la fenetre contextuelle de choix de fichiers
     */
    @FXML
    public void setOpenMenuAction(ActionEvent e) {
        file = fileChooser.showOpenDialog(new Stage());
        try {
            String filename = file.getName();
            String extension = Optional.of(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1)).get();
            if (extension.equals("jpg") || extension.equals("png") || extension.equals("jpeg") || extension.equals("bmp")) {
                System.out.println("hey");
            }
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    public void setNewMenuAction(Event actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fichiers d'image (*.jpg, *.png, *.bmp)", "*.jpg", "*.png", "*.jpeg", "*.bmp"));
            File file = fileChooser.showOpenDialog(new Stage());
            Image base = new Image(String.valueOf(file.toURI()));
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("../ressources/fxmlFiles/canvas.fxml"));
                Parent pane = loader.load();
                this.controllerCanvas = loader.getController();
                this.canvas = this.controllerCanvas.getCanvas();
                this.model = new ProjectModel(base, this.canvas.getGraphicsContext2D());
                this.controllerCanvas.init(this.model, this.primaryStage);
                this.mainPane.getChildren().add(pane);
                this.mainPane.setMaxHeight(base.getHeight());
                this.mainPane.setMaxWidth(base.getWidth());
                this.canvas.setWidth(base.getWidth());
                this.canvas.setHeight(base.getHeight());


                loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("../ressources/fxmlFiles/layers.fxml"));
                Parent pane2 = loader.load();
                this.controllerLayers = loader.getController();
                this.controllerLayers.init(this.model,this.canvas,this.up,this.down,this);
                this.layersPane.getChildren().add(pane2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.layersMenuBar.setDisable(false);
            this.layersModifyBar.setDisable(false);
            this.newShapeButton.setDisable(false);
            this.exportButton.setDisable(false);
            this.newImageButton.setDisable(false);
            this.zoomMinus.setDisable(false);
            this.hboxLayersModify.setDisable(false);
            this.zoomPlus.setDisable(false);
            this.newTextButton.setDisable(false);

            this.model.paintLayers();
        } catch (NullPointerException ignored) {
        }
    }

    @FXML
    public void quitApp(ActionEvent actionEvent) {
        this.primaryStage.close();
        //TODO ajouter etes vous sur de quitter etc
    }

    /**
     * Ouvre la fenetre contextuelle d'ajout de formes
     */
    @FXML
    public void addShapeMenu(ActionEvent actionEvent) {
        String path = "../ressources/fxmlFiles/shapes.fxml";
        String name = "Ajouter une Forme";
        FXMLLoader loader = setNewStage(path, name);
        assert loader != null;
        ControllerShapesMenu controllerShapesMenu = loader.getController();
        controllerShapesMenu.init(this.model, this.controllerCanvas, this.canvas);
    }

    /**
     * Crée une nouvelle fenetre
     *
     * @param path chemin vers le fichier FXML
     * @param name nom de la fenetre
     * @return FXMLLoader
     */
    public FXMLLoader setNewStage(String path, String name) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.setRoot(root);
            stage.setTitle(name);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
            return loader;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    public void exportToJpg(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("fichier jpeg (*.jpeg)", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) this.canvas.getWidth(), (int) this.canvas.getHeight());
                canvas.snapshot(new SnapshotParameters(), writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage,"jpeg", file);
            } catch (IOException ignored) {
            }
        }
    }

    @FXML
    public void exportToBmp(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("fichier bmp (*.bmp)", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) this.canvas.getWidth(), (int) this.canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "bmp", file);
            } catch (IOException ignored) {
            }
        }
    }

    @FXML
    public void exportToPng(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("fichier png (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) this.canvas.getWidth(), (int) this.canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ignored) {
            }
        }
    }

    @FXML
    public void helpButtonAction(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/sukaizer/Mobipic").toURI());
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    @FXML
    public void addImageLayer(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fichiers d'image (*.jpg, *.png, *.bmp)", "*.jpg", "*.png", "*.jpeg", "*.bmp"));
            File file = fileChooser.showOpenDialog(new Stage());
            Image base = new Image(String.valueOf(file.toURI()));
            model.Image image = new model.Image(base,0,0,this.canvas.getGraphicsContext2D());
            this.model.addLayer(image);
            this.model.paintLayers();
        } catch (NullPointerException ignored){}
    }

    @FXML
    public void addTextMenu(ActionEvent actionEvent) {
        String path = "../ressources/fxmlFiles/text.fxml";
        String name = "Ajouter un texte";
        FXMLLoader loader = setNewStage(path, name);
        assert loader != null;
        ControllerTextMenu controllerText = loader.getController();
        controllerText.init(this.model, this.canvas);
    }

    @FXML
    public void addFilter(ActionEvent actionEvent) {
        String path = "../ressources/fxmlFiles/filters.fxml";
        String name = "Appliquer un filtre";
        FXMLLoader loader = setNewStage(path, name);
        assert loader != null;
        ControllerFilters controller = loader.getController();
        controller.init(this.model, this.controllerCanvas);
    }

    @FXML
    public void deleteLayer(Event actionEvent) {
        try {
            this.model.getLayers().removeIf(layer -> layer.isFocused() && !layer.isBaseLayer());
        } catch(Exception ignored) {}
        this.controllerCanvas.clear();
        this.model.paintLayers();
    }

    @FXML
    public void moveLayer(ActionEvent actionEvent) {
        try {
            for (Layer layer: this.model.getLayers()) {
                layer.resetModifiers();
                if (layer.isFocused() && !layer.isBaseLayer()) {
                    layer.setMoving(true);
                    this.model.setEditingLayer(layer);
                }
            }
        } catch(Exception ignored) {}
    }

    @FXML
    public void resizeLayer(ActionEvent actionEvent) {
        try {
            for (Layer layer: this.model.getLayers()) {
                layer.resetModifiers();
                if (layer.isFocused() && !layer.isBaseLayer()) {
                    layer.setResizing(true);
                    this.model.setEditingLayer(layer);
                    if (layer instanceof Triangle){
                        Square l1 = new Square(layer.getX() - 5, layer.getY() - 5,10,this.canvas.getGraphicsContext2D());
                        Square l2 = new Square(((Triangle) layer).getX2() - 5, ((Triangle) layer).getY2() - 5,10,this.canvas.getGraphicsContext2D());
                        Square l3 = new Square(((Triangle) layer).getX3() - 5, ((Triangle) layer).getY3() - 5,10,this.canvas.getGraphicsContext2D());
                        l1.setFilled(true);
                        l2.setFilled(true);
                        l3.setFilled(true);
                        l1.paint();
                        l2.paint();
                        l3.paint();
                    } else if(layer instanceof Line){
                        Square l1 = new Square(layer.getX() - 5, layer.getY() - 5,10,this.canvas.getGraphicsContext2D());
                        Square l2 = new Square(((Line) layer).getX2() - 5, ((Line) layer).getY2() - 5,10,this.canvas.getGraphicsContext2D());
                        l1.setFilled(true);
                        l2.setFilled(true);
                        l1.paint();
                        l2.paint();
                    } else {
                        Square l1 = new Square(layer.getX() - 5, layer.getY() - 5,10,this.canvas.getGraphicsContext2D());
                        l1.setFilled(true);
                        l1.paint();
                    }
                }
            }
        } catch(Exception ignored) {}
    }

    @FXML
    public void duplicateLayer(ActionEvent actionEvent) {
        try {
            for (int i = 0; i < this.model.getLayers().size(); i++) {
                if (this.model.getLayers().get(i).isFocused() && !this.model.getLayers().get(i).isBaseLayer()){
                    if (this.model.getLayers().get(i) instanceof Line) {
                        Line line = new Line((Line) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,line);
                    } else if (this.model.getLayers().get(i) instanceof Square) {
                        Square square = new Square((Square) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,square);
                    } else if (this.model.getLayers().get(i) instanceof Rectangle) {
                        Rectangle rectangle = new Rectangle((Rectangle) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,rectangle);
                    } else if (this.model.getLayers().get(i) instanceof Circle) {
                        Circle circle = new Circle((Circle) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,circle);
                    } else if (this.model.getLayers().get(i) instanceof Triangle) {
                        Triangle triangle = new Triangle((Triangle) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,triangle);
                    } else if (this.model.getLayers().get(i) instanceof model.Image) {
                        model.Image image = new model.Image((model.Image) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,image);
                    } else if (this.model.getLayers().get(i) instanceof Ellipse) {
                        Ellipse ellipse = new Ellipse((Ellipse) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,ellipse);
                    } else if (this.model.getLayers().get(i) instanceof Text) {
                        Text text = new Text((Text) this.model.getLayers().get(i));
                        this.model.getLayers().add(i+1,text);
                    }
                }
            }
            this.model.paintLayers();
        } catch(Exception ignored) {}
    }

    @FXML
    public void sendBack(ActionEvent actionEvent) {
        for (int i = 0; i < this.model.getLayers().size(); i++) {
            if (this.model.getLayers().get(i).isFocused() && !this.model.getLayers().get(i).isBaseLayer()){
                Collections.swap(this.model.getLayers(),i,i-1);
                this.controllerCanvas.clear();
                this.model.paintLayers();
                this.model.getLayers().get(i-1).setFocused(false);
                this.controllerLayers.clearSelection();
                this.up.setDisable(true);
                this.down.setDisable(true);
                return;
            }
        }
    }

    @FXML
    public void sendForward(ActionEvent actionEvent) {
        for (int i = 0; i < this.model.getLayers().size(); i++) {
            if (this.model.getLayers().get(i).isFocused() && !this.model.getLayers().get(i).isBaseLayer()){
                System.out.println("down");
                Collections.swap(this.model.getLayers(),i,i+1);
                this.controllerCanvas.clear();
                this.model.paintLayers();
                this.model.getLayers().get(i).setFocused(false);
                this.model.getLayers().get(i+1).setFocused(false);
                this.controllerLayers.clearSelection();
                this.up.setDisable(true);
                this.down.setDisable(true);
                return;
            }
        }
    }

    @FXML
    public void zoomPlus(ActionEvent actionEvent) {
        this.canvas.setScaleX(this.canvas.getScaleX()*1.2);
        this.canvas.setScaleY(this.canvas.getScaleY()*1.2);
    }

    @FXML
    public void zoomMinus(ActionEvent actionEvent) {
        this.canvas.setScaleX(this.canvas.getScaleX()*0.8);
        this.canvas.setScaleY(this.canvas.getScaleY()*0.8);
    }

    @FXML
    public void keyAction(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            this.controllerLayers.getLayersList().getSelectionModel().clearSelection();
            try {
                for (Layer l : this.model.getLayers()) {
                    l.setFocused(false);
                }
                this.controllerLayers.clear();
                this.model.paintLayers();
            }catch(Exception ignored){}
        }
        if (this.keyCombinationControlN.match(keyEvent)) {
            this.setNewMenuAction(keyEvent);
        } else if (this.keyCombinationControlSuppr.match(keyEvent)) {
            this.deleteLayer(keyEvent);
        } else if (this.keyCombinationControlZ.match(keyEvent)) {
            this.undoAction(keyEvent);
        } else if (this.keyCombinationControlY.match(keyEvent)) {
            this.redoAction(keyEvent);
        }
    }

    public void undoAction(Event actionEvent) {
    }

    public void redoAction(Event actionEvent) {
    }

    public ControllerCanvas getControllerCanvas() {
        return controllerCanvas;
    }


    @FXML
    public void saveProject(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            boolean bool = file.mkdir();
            FileWriter myfile = new FileWriter(file.getAbsolutePath() + File.separator + file.getName() + ".mbpc");
            int i = 0;
            StringBuilder save = new StringBuilder();
            for (Layer layer: this.model.getLayers()) {
                if (layer instanceof model.Image){
                    Image image = ((model.Image) layer).getImage();
                    File outputFile = new File(file.getAbsolutePath() + File.separator + "ours" + i + ".png");
                    BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
                    try {
                        ImageIO.write(bImage, "png", outputFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    save.append(layer.save());
                    save.append(" ").append(outputFile.getAbsolutePath());
                    save.append("\n");
                    i++;
                } else {
                    save.append(layer.save()).append("\n");
                }
            }
            myfile.write(String.valueOf(save));
            myfile.close();
        }
    }
}
