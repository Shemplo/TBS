package ru.shemplo.tbs.gfx.table;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import ru.shemplo.tbs.entity.LinkedObject;

public class TBSToggleTableCell <F> extends TBSTableCell <F, LinkedObject <Boolean>> {
    
    private final CheckBox checkBox;
    
    public TBSToggleTableCell (
        BiConsumer <LinkedObject <Boolean>, Boolean> onToggle, 
        Consumer <TBSTableCell <F, LinkedObject <Boolean>>> highlighter
    ) {
        super ((__, ___) -> "", highlighter, Pos.CENTER);
        
        checkBox = new CheckBox ();
        checkBox.selectedProperty ().addListener ((__, ___, selected) -> {
            onToggle.accept (getItem (), selected);
        });
    }
    
    @Override
    protected void updateItem (LinkedObject <Boolean> item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            checkBox.setSelected (item.getObject ());
            setGraphic (checkBox);
            
            getStringValue (item, true);
        }
    }
    
}
