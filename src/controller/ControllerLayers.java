package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import model.Layer;
import model.ProjectModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerLayers implements Initializable {
    public ListView<Layer> layersList;
    private ProjectModel model;
    private ObservableList<Layer> layers;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void init(ProjectModel model) {
        this.model = model;
        this.layers = FXCollections.observableArrayList(this.model.getLayerArrayList());
        this.layersList = new ListView<>(this.layers);
    }
}
