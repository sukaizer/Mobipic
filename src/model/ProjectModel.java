package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class ProjectModel {
    private ArrayList<Layer> layerArrayList;
    private ShapeToDraw shapeToDraw;
    private GraphicsContext graphicsContext;

    public ProjectModel() {
        this.shapeToDraw = ShapeToDraw.nothing;
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
        if(this.graphicsContext == null){
            this.graphicsContext = layerArrayList.get(0).getGraphicsContext();
        }
        this.graphicsContext.clearRect(0,0,1000,1000);
        for (Layer layer:this.layerArrayList) {
            layer.paint();
        }
    }
}
