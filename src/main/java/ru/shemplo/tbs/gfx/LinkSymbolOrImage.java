package ru.shemplo.tbs.gfx;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString (callSuper = true)
public class LinkSymbolOrImage extends SymbolOrImage {
    
    private final String link;
    
    protected LinkSymbolOrImage (String symbol, Image image, String link) {
        super (symbol, image);
        this.link = link;
    }
    
    public static SymbolOrImage symbol (String symbol, String link) {
        return new LinkSymbolOrImage (symbol, null, link);
    }
    
    public static SymbolOrImage image (Image image, String link) {
        return new LinkSymbolOrImage (null, image, link);
    }
    
}
