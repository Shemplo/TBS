package ru.shemplo.tbs.gfx;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import ru.shemplo.tbs.TBSUtils;

public class TBSToggleTableCell <T> extends TBSTableCell <T, Boolean> {
    
    private CheckBox checkBox;
    
    public TBSToggleTableCell (ObservableList <?> changeTrigger, Predicate <T> isActive, BiConsumer <T, Boolean> doOnChange) {
        super ((__, value) -> TBSUtils.mapIfNN (isActive, p -> p.test (value), false), null);
        setAlignment (Pos.CENTER);
        
        checkBox = new CheckBox ();
        checkBox.selectedProperty ().addListener ((__, ___, selected) -> {
            TBSUtils.doIfNN (doOnChange, c -> c.accept (getItem (), selected));
        });
        changeTrigger.addListener ((ListChangeListener <Object>) (__ -> {
            final var item = getItem ();
            if (item != null) {
                checkBox.setSelected (converter.apply (
                    getTableRow (), item
                ));
            }
        }));
    }
    
    @Override
    protected void updateItem (T item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            checkBox.setSelected (converter.apply (getTableRow (), item));
            setGraphic (checkBox);
        }
    }
    
    @Override
    protected String getStringValue (T item, boolean updateHighlights) {
        return "";
    }
    
}
