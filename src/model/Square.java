package model;

import javafx.scene.canvas.GraphicsContext;

public class Square extends Layer{

    private double side;

    Square(double x, double y, double s, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.side = s;
    }

    double getSide(){
        return this.side;
    }

    void setSide(double s) {
        this.side = s;
    }

    @Override
    public void paint() {
        if(this.isFilled){
            this.graphicsContext.fillRect(this.x, this.y, this.side, this.side);
        } else{
            this.graphicsContext.strokeRect(this.x, this.y, this.side, this.side);
        }
    }

    @Override
    public boolean isIn(double x, double y) {
        return x >= this.x && x <= this.x + this.side && y >= this.y && y <= this.y + this.side;
    }
}
