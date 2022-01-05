package ru.shemplo.tbs.gfx.table;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import ru.shemplo.tbs.entity.LinkedObject;

public class TBSSelectTableCell <F, S extends Enum <S>> extends TBSTableCell <F, LinkedObject <S>> {
    
    private final ChoiceBox <S> select;
    
    public TBSSelectTableCell (
        Class <S> enumeration, BiConsumer <LinkedObject <S>, S> onSelection,
        Consumer <TBSTableCell <F, LinkedObject <S>>> highlighter,
        Pos alignment
    ) {
        super ((__, ___) -> null, highlighter, alignment);
        setPadding (new Insets (0.0));
        
        select = new ChoiceBox <> (FXCollections.observableArrayList (enumeration.getEnumConstants ()));
        select.valueProperty ().addListener ((__, ___, selected) -> {
            onSelection.accept (getItem (), selected);
        });
    }
    
    @Override
    protected void updateItem (LinkedObject <S> item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            //if (select.getSelectionModel ().getSelectedItem () != item.getObject ()) {                
            select.getSelectionModel ().select (item.getObject ());
            //}
            
            setGraphic (select);
        } else {
            setGraphic (null);
        }
    }
    
}
