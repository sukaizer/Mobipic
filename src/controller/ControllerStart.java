package controller;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerStart implements Initializable {
    public ImageView image;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image im = new Image("ressources/images/iconeMobi.png");
        this.image.setImage(im);
    }

}
