package controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Layer;
import model.ProjectModel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
    public ButtonBar layersModifyBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
    public void setNewMenuAction(ActionEvent actionEvent) {
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
                this.controllerCanvas.init(this.model);
                this.mainPane.getChildren().add(pane);
                this.mainPane.setMinHeight(base.getHeight());
                this.mainPane.setMinWidth(base.getWidth());
                this.canvas.setWidth(base.getWidth());
                this.canvas.setHeight(base.getHeight());

                loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("../ressources/fxmlFiles/layers.fxml"));
                Parent pane2 = loader.load();
                this.controllerLayers = loader.getController();
                this.controllerLayers.init(this.model);
                this.layersPane.getChildren().add(pane2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.layersMenuBar.setDisable(false);
            this.layersModifyBar.setDisable(false);
            this.newShapeButton.setDisable(false);
            this.exportButton.setDisable(false);
            this.newImageButton.setDisable(false);
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
    private FXMLLoader setNewStage(String path, String name) {
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
                new FileChooser.ExtensionFilter("fichier jpg (*.jpg)", "*.jpg");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) this.canvas.getWidth(), (int) this.canvas.getHeight());
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "jpg", file);
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
        controllerText.init(this.model, this.controllerCanvas, this.canvas);
    }

    @FXML
    public void deleteLayer(ActionEvent actionEvent) {
        try {
            this.model.getLayers().removeIf(Layer::isFocused);
        } catch(Exception ignored) {}
        this.model.paintLayers();
    }

    @FXML
    public void moveLayer(ActionEvent actionEvent) {
    }

    @FXML
    public void resizeLayer(ActionEvent actionEvent) {
    }
}
