package ru.shemplo.tbs.gfx;

import java.util.function.BiConsumer;

import com.panemu.tiwulfx.control.NumberField;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.CustomValueHolder;

public class TBSEditableCell <F extends CustomValueHolder <Integer>, S> extends TBSTableCell <F, S> {

    private final NumberField <Integer> field;
    
    public TBSEditableCell (BiConsumer <Integer, F> onValueChanged, double paddings) {
        super (null, null, Pos.BASELINE_LEFT);
        
        field = new NumberField <> (Integer.class);
        field.setPadding (new Insets (paddings, 4, paddings, 4));
        field.valueProperty ().addListener ((__, ___, value) -> {
            TBSUtils.doIfNN (getItem (), i -> {
                i.setCustomValue (value);
                
                onValueChanged.accept (value, i);
            });
        });
    }
    
    @Override
    protected void updateItem (F item, boolean empty) {
        super.updateItem (item, empty);
        
        if (item != null) {
            field.setValue (item.getCustomValue ());
            setGraphic (field);
            setText (null);
        }
    }
    
    @Override
    protected String getStringValue (F item, boolean updateHighlights) {
        return "";
    }
    
}
