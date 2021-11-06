package ru.shemplo.tbs.gfx.table;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import ru.shemplo.tbs.gfx.LinkFlag;

public class TBSToggleTableCell <F> extends TBSTableCell <F, LinkFlag> {
    
    private final CheckBox checkBox;
    
    public TBSToggleTableCell (BiConsumer <LinkFlag, Boolean> onToggle, Consumer <TBSTableCell <F, LinkFlag>> highlighter) {
        super ((__, ___) -> "", highlighter, Pos.CENTER);
        
        checkBox = new CheckBox ();
        checkBox.selectedProperty ().addListener ((__, ___, selected) -> {
            onToggle.accept (getItem (), selected);
        });
    }
    
    @Override
    protected void updateItem (LinkFlag item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            checkBox.setSelected (item.isFlag ());
            setGraphic (checkBox);
            
            getStringValue (item, true);
        }
    }
    
}
