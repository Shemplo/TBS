package ru.shemplo.tbs.gfx;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString (callSuper = true)
public class LinkedSymbolOrImage extends LinkedObject <SymbolOrImage> {
    
    private final String link;
    
    protected LinkedSymbolOrImage (String symbol, Image image, String link) {
        super (link, new SymbolOrImage (symbol, image));
        this.link = link;
    }
    
    public static LinkedSymbolOrImage symbol (String symbol, String link) {
        return new LinkedSymbolOrImage (symbol, null, link);
    }
    
    public static LinkedSymbolOrImage image (Image image, String link) {
        return new LinkedSymbolOrImage (null, image, link);
    }
    
}
