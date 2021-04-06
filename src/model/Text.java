package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class Text extends Layer{
    private String text;
    private TextAlignment textAlignment;
    private Font font;
    private FontWeight fontWeight;
    private double size;

    public Text(double x, double y, String text, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.text = text;
        this.textAlignment = TextAlignment.LEFT;
        this.fontWeight = FontWeight.NORMAL;
        this.size = 10;
        this.font = Font.font("Calibri",this.fontWeight,this.size);
    }

    public void setText(String text){
        this.text = text;
    }

    public void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    public void setFont(Font font){
        this.font = font;
    }

    public void setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
    }

    @Override
    public void paint() {
        this.graphicsContext.setFont(this.font);
        this.graphicsContext.fillText(this.text,this.x,this.y);

    }

    @Override
    public boolean isIn(double x, double y) {
        return false;
    }
}
