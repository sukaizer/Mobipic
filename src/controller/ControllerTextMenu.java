package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
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
    public ChoiceBox<String> fontPicker = new ChoiceBox<>(FXCollections.observableArrayList());
    @FXML
    public Slider fontSize;
    @FXML
    public Label label;
    @FXML
    public ToggleButton italic;
    @FXML
    public ToggleButton bold;

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
        this.fontPicker.getItems().addAll("Verdana","Times New Roman","Arial");
        this.fontPicker.getSelectionModel().select("Verdana");
        this.label.setFont(Font.font(this.fontPicker.getValue(),this.fontSize.getValue()));
        label.setTextFill(this.colorPicker.getValue());
    }

    public void validate(ActionEvent actionEvent) {
        if (this.bold.isSelected()){
            this.text.setFontWeight(FontWeight.BOLD);
        } else {
            this.text.setFontWeight(FontWeight.NORMAL);
        }
        if (this.italic.isSelected()){
            this.text.setFontPosture(FontPosture.ITALIC);
        } else {
            this.text.setFontPosture(FontPosture.REGULAR);
        }

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

    @FXML
    public void actualizeSampleText(MouseEvent e) {
        actualize();
    }

    @FXML
    public void setBold(ActionEvent actionEvent) {
        actualize();
    }

    @FXML
    public void setItalic(ActionEvent actionEvent) {
        actualize();
    }

    @FXML
    public void actualizeFont(MouseEvent mouseEvent) {
        actualize();
    }

    @FXML
    public void color(MouseEvent mouseEvent) {
        actualize();
    }

    public void actualize(){
        if (this.bold.isSelected() && this.italic.isSelected()){
            this.label.setFont(Font.font(this.fontPicker.getValue(),FontWeight.BOLD,FontPosture.ITALIC,this.fontSize.getValue()));
        } else if (!this.bold.isSelected() && !this.italic.isSelected()) {
            this.label.setFont(Font.font(this.fontPicker.getValue(),FontWeight.NORMAL,FontPosture.REGULAR,this.fontSize.getValue()));
        } else if (!this.bold.isSelected() && this.italic.isSelected()){
            this.label.setFont(Font.font(this.fontPicker.getValue(),FontWeight.NORMAL,FontPosture.ITALIC,this.fontSize.getValue()));
        } else if (this.bold.isSelected() && !this.italic.isSelected()){
            this.label.setFont(Font.font(this.fontPicker.getValue(),FontWeight.BOLD,FontPosture.REGULAR,this.fontSize.getValue()));
        }
        label.setTextFill(this.colorPicker.getValue());
    }

}
