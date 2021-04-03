package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class ProjectModel {
    private ArrayList<Layer> layerArrayList;
    private ShapeToDraw shapeToDraw;
    private GraphicsContext graphicsContext;
    private boolean editing;
    private javafx.scene.image.Image baseImage;
    private Image baseLayer;

    public ProjectModel(javafx.scene.image.Image baseImage,GraphicsContext graphicsContext) {
        this.shapeToDraw = ShapeToDraw.nothing;
        this.layerArrayList = new ArrayList<>();
        this.editing = false;
        this.baseImage = baseImage;
        this.graphicsContext = graphicsContext;
        this.baseLayer = new Image(baseImage,0,0,this.graphicsContext);
    }

    public ArrayList<Layer> getLayerArrayList() {
        return layerArrayList;
    }

    public void addLayer(Layer layer){
        this.layerArrayList.add(layer);
    }

    public void deleteLastLayer(){
        this.layerArrayList.remove(this.layerArrayList.size()-1);
    }

    public ShapeToDraw getShapeToDraw() {
        return shapeToDraw;
    }

    public void setShapeToDraw(ShapeToDraw shapeToDraw) {
        this.shapeToDraw = shapeToDraw;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    /**
     * Dessine toutes les formes présentes
     */
    public void paintLayers(){
        this.baseLayer.paint();
        for (Layer layer:this.layerArrayList) {
            layer.paint();
        }
    }
}
