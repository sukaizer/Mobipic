package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class Text extends Layer{
    private String text;
    private TextAlignment textAlignment;
    private String font;
    private Font finalFont;
    private FontWeight fontWeight;
    private FontPosture fontPosture;
    private double size;

    public Text(double x, double y, String text, GraphicsContext graphicsContext) {
        super(x, y, graphicsContext);
        this.text = text;
        this.textAlignment = TextAlignment.LEFT;
        this.fontWeight = FontWeight.NORMAL;
        this.fontPosture = FontPosture.REGULAR;
        this.size = 10;
        this.font = "Calibri";
        this.finalFont = Font.font(this.font,this.fontWeight,this.fontPosture,this.size);
        this.isFilled = true;
        this.color = Color.BLACK;
    }

    public Text(Text text) {
        super(text.x,text.y,text.graphicsContext);
        this.text = text.text;
        this.textAlignment = text.textAlignment;
        this.fontWeight = text.fontWeight;
        this.fontPosture = text.fontPosture;
        this.size = text.size;
        this.finalFont = text.finalFont;
        this.isFilled = true;
        this.color = text.color;

    }

    public void setText(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getFont() {
        return font;
    }

    @Override
    public void setColor(Paint color) {
        this.color = color;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignment = textAlignment;
    }

    public void setFont(String font){
        this.font = font;
    }

    public void setFinalFont(Font finalFont) {
        this.finalFont = finalFont;
    }

    public void setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
    }

    public void setFontPosture(FontPosture fontPosture) {
        this.fontPosture = fontPosture;
    }

    public FontWeight getFontWeight() {
        return fontWeight;
    }

    public FontPosture getFontPosture() {
        return fontPosture;
    }

    public double getSize() {
        return size;
    }

    @Override
    public void paint() {
        this.graphicsContext.setFont(this.finalFont);
        this.graphicsContext.setTextAlign(this.textAlignment);
        this.graphicsContext.setFill(this.color);
        this.graphicsContext.fillText(this.text,this.x,this.y);
    }

    @Override
    public boolean isIn(double x, double y) {
        double xc = this.x - 20 + 15;
        double yc = this.y - 20 + 15;
        return (x-xc)*(x-xc)+(y-yc)*(y-yc) <= 15*15;
    }

    @Override
    public String toString() {
        return "Texte";
    }

    @Override
    public Circle setSamePositions() {
        Circle layer1 = new Circle(this.x - 20,this.y - 20, 15,graphicsContext);
        layer1.setFilled(false);
        layer1.setLineWidth(6);
        layer1.setColor(new Color(0,0,0,1));
        return layer1;
    }

    @Override
    public String save() {
        return "Texte " + this.x + " " + this.y + " " + this.color + " " + this.font + " obo12" + this.fontPosture + " " + this.fontWeight + " " + this.text;
    }
}
