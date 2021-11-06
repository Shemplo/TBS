package ru.shemplo.tbs.gfx;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class SymbolOrImage {
    
    private final String symbol;
    private final Image image;
    
    public boolean isImage () {
        return image != null;
    }
    
    public boolean isSymbol () {
        return symbol != null;
    }
    
    public static SymbolOrImage symbol (String symbol) {
        return new SymbolOrImage (symbol, null);
    }
    
    public static SymbolOrImage image (Image image) {
        return new SymbolOrImage (null, image);
    }
    
}
