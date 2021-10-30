package ru.shemplo.tbs.gfx;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import lombok.Getter;
import ru.shemplo.tbs.TBSUtils;

public class TBSTableCell <F, S> extends TableCell <F, F> {
    
    private final BiConsumer <TBSTableCell <F, S>, S> highlighter;
    private final Function <F, S> converter;
    
    @Getter
    private Background defaultBackground;
    
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass ("hover");
    
    public TBSTableCell (Function <F, S> converter, BiConsumer <TBSTableCell <F, S>, S> highlighter) {
        this.converter = converter; this.highlighter = highlighter;
        
        hoverProperty ().addListener ((__, ___, hovered) -> {
            if (getItem () == null) { return; }
            
            getTableRow ().getChildrenUnmodifiable ().forEach (cell -> {
                cell.pseudoClassStateChanged (HOVER_PSEUDO_CLASS, hovered);
            });
        });
        
        setOnMouseClicked (me -> {
            if (getItem () != null && me.getClickCount () == 2) {
                final var content = new ClipboardContent ();
                content.putString (getStringValue (getItem (), false));
                Clipboard.getSystemClipboard ().setContent (content);
            }
        });
    }
    
    public TBSTableCell (Function <F, S> converter, BiConsumer <TBSTableCell <F, S>, S> highlighter, Pos textAlignment) {
        this (converter, highlighter);
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
        
        setText (getStringValue (item, true));
        setGraphic (null);
    }
    
    private String getStringValue (F item, boolean updateHighlights) {
        final var value = converter.apply (item);
        if (updateHighlights) {
            TBSUtils.doIfNN (highlighter, h -> h.accept (this, value));
        }
        if (value instanceof Number n) {
            if (value instanceof Double || value instanceof Float) {                
                return String.format ("%.2f", n);
            } else {
                return String.format ("%d", n);                
            }
        } else {            
            return String.valueOf (value);
        }
    }
    
}
