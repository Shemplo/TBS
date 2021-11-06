package ru.shemplo.tbs.gfx.table;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import lombok.Getter;
import ru.shemplo.tbs.TBSUtils;

public class TBSTableCell <F, S> extends TableCell <F, S> {
    
    protected final BiFunction <TableRow <F>, S, String> converter;
    protected final Consumer <TBSTableCell <F, S>> highlighter;
    
    @Getter
    private Background defaultBackground;
    
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass ("hover");
    
    protected TBSTableCell (BiFunction <TableRow <F>, S, String> converter, Consumer <TBSTableCell <F, S>> highlighter) {
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
    
    public TBSTableCell (
        BiFunction <TableRow <F>, S, String> converter, 
        Consumer <TBSTableCell <F, S>> highlighter, 
        Pos textAlignment
    ) {
        this (converter, highlighter);
        setAlignment (textAlignment);
    }
    
    @Override
    protected void updateItem (S item, boolean empty) {
        if (item == getItem ()) { return; }
        super.updateItem (item, empty);
        
        if (item == null) {
            setGraphic (null);
            setText (null);
            return;
        }
        
        setText (getStringValue (item, true));
        setGraphic (null);
    }
    
    protected String getStringValue (S item, boolean updateHighlights) {
        if (updateHighlights) {
            TBSUtils.doIfNN (highlighter, h -> h.accept (this));
        }
        
        if (item instanceof Number n) {
            if (item instanceof Double || item instanceof Float) {                
                return String.format ("%.2f", n);
            } else {
                return String.format ("%d", n);
            }
        } else if (converter != null) {            
            return converter.apply (getTableRow (), item);
        } else {
            return null;
        }
    }
    
}
