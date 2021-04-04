package controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerShapesMenu implements Initializable {
    @FXML public ColorPicker colorPicker;
    @FXML public CheckBox fillCheck;
    @FXML public Button validateButton;
    @FXML public Slider slider;
    @FXML public Label sliderLabel;
    @FXML public Label descriptionLabel;
    private ProjectModel model;
    private Canvas canvas;
    private ControllerCanvas controllerCanvas;
    private Layer currentLayer;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sliderLabel.textProperty().bind(
                Bindings.format(
                        "%.2f",
                        slider.valueProperty()
                )
        );
        this.descriptionLabel.setVisible(false);
    }

    /**
     * Initialise la classe en lui passant le modèle
     * @param model
     */
    public void init(ProjectModel model,ControllerCanvas controllerCanvas,Canvas canvas){
        this.model = model;
        this.controllerCanvas = controllerCanvas;
        this.canvas = canvas;
        this.validateButton.setDisable(true);
    }

    @FXML
    public void drawLine(ActionEvent actionEvent) {
        this.model.setShapeToDraw(ShapeToDraw.Line);
        this.validateButton.setDisable(false);
        this.descriptionLabel.setVisible(true);
        this.descriptionLabel.setText("Appuyez pour set le premier point puis tirez sans relacher");
    }

    public void drawSquare(ActionEvent actionEvent) {
        this.model.setShapeToDraw(ShapeToDraw.Square);
        this.validateButton.setDisable(false);
        this.descriptionLabel.setVisible(true);
        this.descriptionLabel.setText("Appuyez pour set le coin haut gauche puis tirez pour agrandir le carré");
    }

    public void drawRectangle(ActionEvent actionEvent) {
        this.model.setShapeToDraw(ShapeToDraw.Rectangle);
        this.validateButton.setDisable(false);
        this.descriptionLabel.setVisible(true);
        this.descriptionLabel.setText("Appuyez pour set le coin haut gauche puis tirez pour agrandir le rectangle");
    }

    public void drawCircle(ActionEvent actionEvent) {
        this.model.setShapeToDraw(ShapeToDraw.Circle);
        this.validateButton.setDisable(false);
        this.descriptionLabel.setVisible(true);
        this.descriptionLabel.setText("Appuyez pour set le coin haut gauche puis tirez pour agrandir le cercle");
    }

    public void drawTriangle(ActionEvent actionEvent) {
        this.model.setShapeToDraw(ShapeToDraw.Triangle);
        this.validateButton.setDisable(false);
        this.descriptionLabel.setVisible(true);
        this.descriptionLabel.setText("Appuyez trois fois pour set les trois points du triangle");
    }

    public void validateChoice(ActionEvent actionEvent) {
        switch (this.model.getShapeToDraw()) {
            case Line -> this.currentLayer = new Line(0,0,0,0, this.canvas.getGraphicsContext2D());
            case Square -> this.currentLayer = new Square(0,0,0, this.canvas.getGraphicsContext2D());
            case Rectangle -> this.currentLayer = new Rectangle(0,0,0,0, this.canvas.getGraphicsContext2D());
            case Circle -> this.currentLayer = new Circle(0,0,0, this.canvas.getGraphicsContext2D());
            case Triangle -> this.currentLayer = new Triangle(0,0,0,0, 0,0,this.canvas.getGraphicsContext2D());
        }
        this.currentLayer.setFilled(this.fillCheck.isSelected());
        this.currentLayer.setLineWidth(this.slider.getValue());
        this.currentLayer.setColor(this.colorPicker.getValue());
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        this.controllerCanvas.init(this.currentLayer);
        this.model.setEditing(true);
        stage.close();
    }
}
