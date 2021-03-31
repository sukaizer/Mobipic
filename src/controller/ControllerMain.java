package controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControllerMain implements Initializable {
    FileChooser fileChooser;
    File file;

    public void setOpenMenuAction(ActionEvent e) {
        file = fileChooser.showOpenDialog(new Stage());
        try {
            String filename = file.getName();
            String extension = Optional.of(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1)).get();
            if(extension.equals("jpg") || extension.equals("png") || extension.equals("jpeg") || extension.equals("bmp")){
                System.out.println("hey");
            }
        }catch(NullPointerException ignored){}
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
               new FileChooser.ExtensionFilter("Project Files","*.mbpc"),
               new FileChooser.ExtensionFilter("Image Files","*.jpg","*.png","*.jpeg","*.bmp"));
    }
}
