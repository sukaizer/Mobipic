package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.ProjectModel;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerFilters implements Initializable {
    @FXML
    public ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList());
    private Color[] colors;

    private ProjectModel model;
    private ControllerCanvas controllerCanvas;

    public void init(ProjectModel model, ControllerCanvas canvas){
        this.model = model;
        this.controllerCanvas = canvas;
    }

        @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        choiceBox.getItems().addAll("Normal","Midnight Blue","Harlequin Green");
        colors = new Color[]{
                Color.color(0,0,0,0),
                Color.web("#0066ff",0.4),
                Color.web("#00ff00",0.4)
        };
        this.choiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            for (int i = 0; i < model.getLayers().size(); i++) {
                if (model.getLayers().get(i).isFocused()) {
                    model.getLayers().get(i).setColor(colors[t1.intValue()]);
                }
            }
            controllerCanvas.clear();
            model.paintLayers();
        });
    }


    @FXML
    public void closeWindow(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        this.model.setEditing(true);
        stage.close();
    }
}
