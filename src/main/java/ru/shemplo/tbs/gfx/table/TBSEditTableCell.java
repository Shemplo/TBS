package ru.shemplo.tbs.gfx.table;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import ru.shemplo.tbs.gfx.LinkedObject;

public class TBSEditTableCell <F, S, TF extends TextField> extends TBSTableCell <F, LinkedObject <S>> {
    
    private final TF field;
    
    public TBSEditTableCell (
        Function <TBSEditTableCell <F, S, TF>, TF> field, 
        BiFunction <TableRow <F>, LinkedObject <S>, String> converter, 
        Consumer <TBSTableCell <F, LinkedObject <S>>> highlighter
    ) {
        super (converter, highlighter, Pos.CENTER);
        this.field = field.apply (this);
        setPadding (new Insets (0.0));
    }
    
    @Override
    protected void updateItem (LinkedObject <S> item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            if (field.getText () == null || field.getText ().isBlank ()) {                
                field.setText (getStringValue (item, true));
            }
            
            setGraphic (field);
        }
    }
    
}
