package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;

public class ProjectModel {
    private ObservableList<Layer> layers;
    private ShapeToDraw shapeToDraw;
    private GraphicsContext graphicsContext;
    private boolean editing;
    private Layer editingLayer;
    private Layer helpLayer;
    private Image baseLayer;

    public ProjectModel(javafx.scene.image.Image baseImage,GraphicsContext graphicsContext) {
        this.shapeToDraw = ShapeToDraw.nothing;
        this.layers = FXCollections.observableArrayList();
        this.editing = false;
        this.graphicsContext = graphicsContext;
        this.baseLayer = new Image(baseImage,0,0,this.graphicsContext);
        this.baseLayer.setBaseLayer(true);
        this.editingLayer = null;
        this.layers.add(baseLayer);
    }

    public ObservableList<Layer> getLayers() {
        return layers;
    }

    public void addLayer(Layer layer){
        this.layers.add(layer);
    }

    public void deleteLastLayer(){
        this.layers.remove(this.layers.size()-1);
    }

    public ShapeToDraw getShapeToDraw() {
        return shapeToDraw;
    }

    public void setShapeToDraw(ShapeToDraw shapeToDraw) {
        this.shapeToDraw = shapeToDraw;
    }

    public boolean isNotEditing() {
        return !editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public void layersToString(){
        for (Layer l:this.layers) {
            System.out.println(l.getClass().getName());
        }
        System.out.println();
    }

    /**
     * Dessine toutes les formes présentes
     */
    public void paintLayers(){
        //this.baseLayer.paint();
        for (Layer layer:this.layers) {
            layer.paint();
        }
    }

    public void setEditingLayer(Layer editingLayer) {
        this.editingLayer = editingLayer;
    }

    public Layer getEditingLayer() {
        return editingLayer;
    }

    public void resetEditingLayer(){
        this.editingLayer = null;
    }

    public Layer getHelpLayer() {
        return helpLayer;
    }

    public void setHelpLayer(Layer helpLayer) {
        this.helpLayer = helpLayer;
    }

    public void resetHelpLayer(){
        this.helpLayer = null;
    }

}
