package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(ProjectModel model, Canvas canvas) {
        this.model = model;
        this.layersList.setItems(this.model.getLayers());
        this.canvas = canvas;
    }

    @FXML
    public void action(MouseEvent mouseEvent) {
        selectLayer();
    }

    @FXML
    public void actionKey(KeyEvent keyEvent) {
        selectLayer();
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
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

    private void selectLayer() {
        try {
            for (Layer l : this.model.getLayers()) {
                l.setFocused(false);
            }
            this.layersList.getSelectionModel().getSelectedItem().setFocused(true);
            clear();
            this.model.paintLayers();
            for (Layer l : this.model.getLayers()) {
                if (l.isFocused()) {
                    this.model.setHelpLayer(l.setSamePositions());
                    this.model.getHelpLayer().paint();
                }
            }
        }catch(Exception ignored){}
    }

    public void clear() {
        this.canvas.getGraphicsContext2D().clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }


}
