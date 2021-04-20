package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Image;
import model.Layer;
import model.ProjectModel;
import model.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerLayers implements Initializable {
    public ListView<Layer> layersList = new ListView<>();

    @FXML
    public MenuItem filters;
    @FXML
    public MenuItem delete;
    @FXML
    public MenuItem move;
    @FXML
    public MenuItem resize;
    @FXML
    public MenuItem duplicate;

    private ProjectModel model;
    private Canvas canvas;
    private Button up;
    private Button down;
    private ControllerMain controllerMain;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(ProjectModel model, Canvas canvas,Button up, Button down, ControllerMain controllerMain) {
        this.model = model;
        this.layersList.setItems(this.model.getLayers());
        this.canvas = canvas;
        this.up = up;
        this.down = down;
        this.controllerMain = controllerMain;

        this.filters.setDisable(true);
        this.filters.setVisible(false);
        disableContextMenu();

        this.controllerMain.addFilterMenu.setDisable(true);
        this.up.setDisable(true);
        this.down.setDisable(true);
    }

    @FXML
    public void action(MouseEvent mouseEvent) {
        selectLayer();
    }

    @FXML
    public void actionKey(KeyEvent keyEvent) {
        selectLayer();
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            this.up.setDisable(true);
            this.down.setDisable(true);
            this.layersList.getSelectionModel().clearSelection();
            try {
                for (Layer l : this.model.getLayers()) {
                    l.setFocused(false);
                }
                clear();
                this.model.paintLayers();
            }catch(Exception ignored){}
        }
    }

    public void selectLayer() {
        try {
            for (Layer l : this.model.getLayers()) {
                l.setFocused(false);
            }
            this.layersList.getSelectionModel().getSelectedItem().setFocused(true);
            clear();
            this.model.paintLayers();
            int i = 0;
            for (Layer l : this.model.getLayers()) {
                if (l.isFocused()) {
                    if (l instanceof Image){
                        this.filters.setDisable(false);
                        this.filters.setVisible(true);
                        this.controllerMain.addFilterMenu.setDisable(false);
                    } else {
                        this.filters.setDisable(true);
                        this.filters.setVisible(false);
                        this.controllerMain.addFilterMenu.setDisable(true);
                    }
                    if(!l.isBaseLayer()){
                        this.model.setHelpLayer(l.setSamePositions());
                        this.model.getHelpLayer().paint();
                        if (i > 1 && i < this.model.getLayers().size() - 1){ //cas ni premier ni dernier
                            this.up.setDisable(false);
                            this.down.setDisable(false);
                        } else if (i == 1 && this.model.getLayers().size() == 2){//cas premier et dernier
                            this.up.setDisable(true);
                            this.down.setDisable(true);
                        } else if (i > 1 && i == this.model.getLayers().size() - 1){//cas dernier
                            this.up.setDisable(false);
                            this.down.setDisable(true);
                        } else if (i == 1 && i < this.model.getLayers().size() - 1){//cas premier
                            this.up.setDisable(true);
                            this.down.setDisable(false);
                        }
                        this.controllerMain.setDisableItems(false);
                        this.delete.setDisable(false);
                        this.delete.setVisible(true);
                        this.move.setDisable(false);
                        this.move.setVisible(true);
                        this.resize.setDisable(false);
                        this.resize.setVisible(true);
                        this.duplicate.setDisable(false);
                        this.duplicate.setVisible(true);
                    } else {
                        this.up.setDisable(true);
                        this.down.setDisable(true);
                        this.controllerMain.setDisableItems(true);
                        disableContextMenu();
                    }
                    if (l instanceof Text){
                        this.controllerMain.resizeButton.setDisable(true);
                        this.controllerMain.resizeMenuItem.setDisable(true);
                        this.resize.setDisable(true);
                        this.resize.setVisible(false);
                    }
                }
                i++;
            }
        }catch(Exception ignored){}
    }

    public void disableContextMenu() {
        this.delete.setDisable(true);
        this.delete.setVisible(false);
        this.move.setDisable(true);
        this.move.setVisible(false);
        this.resize.setDisable(true);
        this.resize.setVisible(false);
        this.duplicate.setDisable(true);
        this.duplicate.setVisible(false);
    }

    public void clear() {
        this.canvas.getGraphicsContext2D().clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    public ListView<Layer> getLayersList() {
        return layersList;
    }

    public void clearSelection(){
        this.layersList.getSelectionModel().clearSelection();
    }

    @FXML
    public void deleteLayer(ActionEvent actionEvent) {
        this.controllerMain.deleteLayer(actionEvent);
    }

    @FXML
    public void moveLayer(ActionEvent actionEvent) {
        this.controllerMain.moveLayer(actionEvent);
    }

    @FXML
    public void resizeLayer(ActionEvent actionEvent) {
        this.controllerMain.resizeLayer(actionEvent);
    }

    @FXML
    public void duplicateLayer(ActionEvent actionEvent) {
        this.controllerMain.duplicateLayer(actionEvent);
    }

    @FXML
    public void filterMenu(ActionEvent actionEvent) {
        this.controllerMain.addFilter(actionEvent);
    }
}
