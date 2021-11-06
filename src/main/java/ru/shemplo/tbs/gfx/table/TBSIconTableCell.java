package ru.shemplo.tbs.gfx.table;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import ru.shemplo.tbs.gfx.SymbolOrImage;

public class TBSIconTableCell <F> extends TBSTableCell <F, SymbolOrImage> {

    private final BiConsumer <MouseEvent, TBSTableCell <F, SymbolOrImage>> onClick;
    
    public TBSIconTableCell (
        Consumer <TBSTableCell <F, SymbolOrImage>> highlighter, 
        BiConsumer <MouseEvent, TBSTableCell <F, SymbolOrImage>> onClick
    ) {
        super ((__, ___) -> "", highlighter, Pos.CENTER);
        this.onClick = onClick;
    }
    
    @Override
    protected void updateItem (SymbolOrImage item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            Node view = null;
            if (item.isImage ()) {
                view = new ImageView (item.getImage ());
            } else if (item.isSymbol ()) {
                view = new Text (item.getSymbol ());
            }
            
            if (view != null) {
                setGraphic (view);
                
                if (onClick != null) {
                    view.setOnMouseClicked (me -> onClick.accept (me, this));
                }
            }
            
            getStringValue (item, true);
        }
    }
    
}
