package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Layer;
import model.ProjectModel;
import model.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerTextMenu implements Initializable {
    @FXML
    public TextField textField;
    @FXML
    public ColorPicker colorPicker;
    @FXML
    public Button validateButton;
    @FXML
    public ChoiceBox<String> fontPicker;
    @FXML
    public Slider fontSize;

    private ProjectModel model;
    private Canvas canvas;
    private ControllerCanvas controllerCanvas;
    private Layer currentLayer;
    private Text text;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //this.validateButton.setDisable(true);
    }

    public void init(ProjectModel model, ControllerCanvas controllerCanvas, Canvas canvas){
        this.model = model;
        this.controllerCanvas = controllerCanvas;
        this.canvas = canvas;
        this.text = new Text(this.canvas.getWidth()/2,this.canvas.getHeight()/2,"",this.canvas.getGraphicsContext2D());
    }

    @FXML
    public void alignmentLeft(ActionEvent actionEvent) {
        this.text.setTextAlignment(TextAlignment.LEFT);
    }

    @FXML
    public void alignmentCenter(ActionEvent actionEvent) {
        this.text.setTextAlignment(TextAlignment.CENTER);
    }

    @FXML
    public void alignmentRight(ActionEvent actionEvent) {
        this.text.setTextAlignment(TextAlignment.RIGHT);
    }

    @FXML
    public void bold(ActionEvent actionEvent) {
        this.text.setFontWeight(FontWeight.BOLD);
    }

    @FXML
    public void italic(ActionEvent actionEvent) {
        this.text.setFontPosture(FontPosture.ITALIC);
    }

    @FXML
    public void underlined(ActionEvent actionEvent) {
        this.text.setFontWeight(FontWeight.BOLD);
    }

    public void validate(ActionEvent actionEvent) {
        this.text.setText(this.textField.getText());
        this.text.setFont(this.fontPicker.getValue());
        Font finalfont = Font.font(this.text.getFont(),this.text.getFontWeight(),this.text.getFontPosture(),this.text.getSize());
        this.text.setColor(this.colorPicker.getValue());
        this.text.setSize(fontSize.getValue());
        this.text.setFinalFont(finalfont);

        this.model.addLayer(this.text);
        this.model.paintLayers();
    }
}
