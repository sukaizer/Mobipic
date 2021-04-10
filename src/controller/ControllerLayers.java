package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import model.Layer;
import model.ProjectModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerLayers implements Initializable {
    public ListView<Layer> layersList = new ListView<>();
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
                    this.model.setHelpLayer(l.setSamePositions());
                    this.model.getHelpLayer().paint();
                    if (i > 0 && i < this.model.getLayers().size() - 1){
                        this.up.setDisable(false);
                        this.down.setDisable(false);
                    } else if (i == 0 && i == this.model.getLayers().size() - 1){
                        this.up.setDisable(true);
                        this.down.setDisable(true);
                    } else if (i > 0 && i == this.model.getLayers().size() - 1){
                        this.up.setDisable(false);
                        this.down.setDisable(true);
                    } else if (i == 0 && i < this.model.getLayers().size() - 1){
                        this.up.setDisable(true);
                        this.down.setDisable(false);
                    }
                }
                i++;
            }
        }catch(Exception ignored){}
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
}
