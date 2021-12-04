package ru.shemplo.tbs.gfx.component;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;

@Getter
public class TileWithHeader <N extends Node> extends VBox {
    
    private final String header;
    private final N [] nodes;
    
    @SafeVarargs
    public TileWithHeader (String header, N ... nodes) {
        this.header = header; this.nodes = nodes;
        setSpacing (4.0);
        
        getChildren ().add (new Text (header));
        for (final N control : nodes) {            
            getChildren ().add (control);
        }
    }
    
}
