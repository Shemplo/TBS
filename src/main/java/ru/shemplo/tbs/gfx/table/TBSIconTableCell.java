package ru.shemplo.tbs.gfx.table;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import ru.shemplo.tbs.entity.LinkedSymbolOrImage;

public class TBSIconTableCell <F> extends TBSTableCell <F, LinkedSymbolOrImage> {

    private final BiConsumer <MouseEvent, TBSTableCell <F, LinkedSymbolOrImage>> onClick;
    
    public TBSIconTableCell (
        Consumer <TBSTableCell <F, LinkedSymbolOrImage>> highlighter, 
        BiConsumer <MouseEvent, TBSTableCell <F, LinkedSymbolOrImage>> onClick
    ) {
        super ((__, ___) -> "", highlighter, Pos.CENTER);
        this.onClick = onClick;
    }
    
    @Override
    protected void updateItem (LinkedSymbolOrImage item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            Node view = null;
            if (item.getObject ().isImage ()) {
                view = new ImageView (item.getObject ().getImage ());
            } else if (item.getObject ().isSymbol ()) {
                view = new Text (item.getObject ().getSymbol ());
            }
            
            if (view != null) {
                setGraphic (view);
                
                if (onClick != null && item.getObject ().isSymbol ()) {
                    view.setOnMouseClicked (me -> onClick.accept (me, this));
                }
            }
            
            getStringValue (item, true);
        }
    }
    
}
