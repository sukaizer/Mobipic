package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
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
    private Text text;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void init(ProjectModel model, Canvas canvas){
        this.model = model;
        this.canvas = canvas;
        this.text = new Text(this.canvas.getWidth()/2,this.canvas.getHeight()/2,"",this.canvas.getGraphicsContext2D());
        this.fontPicker.getItems().addAll("Calibri","Verdana","Times New Roman");
        this.fontPicker.getSelectionModel().select("Calibri");
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


    public void validate(ActionEvent actionEvent) {
        this.text.setText(this.textField.getText());
        this.text.setSize(this.fontSize.getValue());
        Font finalfont = Font.font(this.fontPicker.getValue(),this.text.getFontWeight(),this.text.getFontPosture(),this.text.getSize());
        this.text.setColor(this.colorPicker.getValue());
        this.text.setFinalFont(finalfont);

        this.model.addLayer(this.text);
        this.model.paintLayers();
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
