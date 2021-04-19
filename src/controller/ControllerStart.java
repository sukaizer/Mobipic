package controller;

import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
