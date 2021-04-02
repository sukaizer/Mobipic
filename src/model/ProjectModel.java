package model;

import java.util.ArrayList;

public class ProjectModel {
    ArrayList<Layer> layerArrayList;

    public ProjectModel() {
        this.layerArrayList = new ArrayList<>();
    }

    public void paintLayers(){
        //erased
        for (Layer layer:this.layerArrayList) {
            layer.paint();
        }
    }
}
