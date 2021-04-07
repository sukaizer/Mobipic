package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.ComboBoxListCell;
import model.Layer;
import model.ProjectModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerLayers implements Initializable {
    public ListView<Layer> layersList = new ListView<>();
    private ProjectModel model;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void init(ProjectModel model) {
        this.model = model;
        this.layersList.setItems(this.model.getLayers());
    }

}
