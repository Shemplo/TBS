package ru.shemplo.tbs.gfx.dep;

import java.util.function.BiConsumer;

import com.panemu.tiwulfx.control.NumberField;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import ru.shemplo.tbs.entity.CustomValueHolder;
import ru.shemplo.tbs.gfx.table.TBSTableCell;

public class TBSEditableCell <F extends CustomValueHolder <Integer>> extends TBSTableCell <F, Integer> {

    private final NumberField <Integer> field;
    
    public TBSEditableCell (BiConsumer <Integer, F> onValueChanged, double paddings) {
        super (null, null, Pos.BASELINE_LEFT);
        
        field = new NumberField <> (Integer.class);
        field.setPadding (new Insets (paddings, 4, paddings, 4));
        /*
        field.valueProperty ().addListener ((__, ___, value) -> {
            TBSUtils.doIfNN (getItem (), i -> {
                i.setCustomValue (value);
                
                onValueChanged.accept (value, i);
            });
        });
        */
        itemProperty ().bindBidirectional (field.valueProperty ());
    }
    
    /*
    @Override
    protected void updateItem (Integer item, boolean empty) {
        super.updateItem (item, empty);
        
        if (item != null) {
            field.setValue (item.getCustomValue ());
            setGraphic (field);
            setText (null);
        }
    }
    */
    
    @Override
    protected String getStringValue (Integer item, boolean updateHighlights) {
        return "";
    }
    
}
