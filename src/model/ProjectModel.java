package model;

import java.util.ArrayList;

public class ProjectModel {
    private ArrayList<Layer> layerArrayList;
    private ShapeToDraw shapeToDraw;

    public ProjectModel() {
        this.shapeToDraw = null;
        this.layerArrayList = new ArrayList<>();
    }

    public ArrayList<Layer> getLayerArrayList() {
        return layerArrayList;
    }

    public void addLayer(Layer layer){
        this.layerArrayList.add(layer);
    }

    public ShapeToDraw getShapeToDraw() {
        return shapeToDraw;
    }

    public void setShapeToDraw(ShapeToDraw shapeToDraw) {
        this.shapeToDraw = shapeToDraw;
    }

    public void paintLayers(){
        //erased
        for (Layer layer:this.layerArrayList) {
            layer.paint();
        }
    }
}
