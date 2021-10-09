package ru.shemplo.tbs.gfx;

import java.util.function.Function;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TBSTableCell <F, S> extends TableCell <F, F> {
    
    private final Function <F, S> converter;
    private final boolean colorizeNumbers;
    
    private static final Background HOVER_BG = new Background (new BackgroundFill (Color.rgb (220, 240, 245, 1.0), null, null));
    private static final Font COLOR_FONT = Font.font ("Consolas", FontWeight.NORMAL, 12.0);
    
    private Background defaultBackground;
    
    public TBSTableCell (Function <F, S> converter, boolean colorizeNumbers) {
        this.converter = converter; this.colorizeNumbers = colorizeNumbers;
        
        hoverProperty ().addListener ((__, ___, hovered) -> {
            if (getItem () == null) { return; }
            
            if (hovered) {
                defaultBackground = getTableRow ().getBackground ();
                getTableRow ().setBackground (HOVER_BG);
            } else {
                getTableRow ().setBackground (defaultBackground);
            }
        });
    }
    
    public TBSTableCell (Function <F, S> converter, boolean colorizeNumbers, Pos textAlignment) {
        this (converter, colorizeNumbers);
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
                
                if (n.doubleValue () > 1e-6) {
                    setTextFill (Color.GREEN);
                } else if (n.doubleValue () < -1e-6) {
                    setTextFill (Color.RED);
                } else {                    
                    setTextFill (Color.BLACK);
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
