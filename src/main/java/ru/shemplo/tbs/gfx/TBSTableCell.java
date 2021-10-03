package ru.shemplo.tbs.gfx;

import java.util.function.Function;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TBSTableCell <F, S> extends TableCell <F, F> {
    
    private final Function <F, S> converter;
    private final boolean colorizeNumbers;
    
    private static final Font COLOR_FONT = Font.font ("Consolas", FontWeight.NORMAL, 12.0);
    
    public TBSTableCell (Function <F, S> converter, boolean colorizeNumbers, Pos textAlignment) {
        this.converter = converter; this.colorizeNumbers = colorizeNumbers;
        setAlignment (textAlignment);
    }
    
    @Override
    protected void updateItem (F item, boolean empty) {
        if (item == getItem ()) { return; }
        super.updateItem (item, empty);
        
        if (item == null) {
            setTooltip (null);
            setGraphic (null);
            setText (null);
            return;
        }
        
        final var value = converter.apply (item);
        if (value instanceof Number n) {
            if (colorizeNumbers) {
                setFont (COLOR_FONT);
                
                if (n.doubleValue () > 1e-4) {                    
                    setTextFill (Color.GREEN);
                } else if (n.doubleValue () < 1e-4) {
                    setTextFill (Color.RED);
                }
            }
            
            if (value instanceof Double || value instanceof Float) {                
                setText (String.format ("%.2f", n));
            } else {
                setText (String.format ("%d", n));                
            }
        } else {            
            setText (String.valueOf (value));
        }
        
        setGraphic (null);
    }
    
}
