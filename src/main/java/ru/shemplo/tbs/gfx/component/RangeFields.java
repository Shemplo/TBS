package ru.shemplo.tbs.gfx.component;

import com.panemu.tiwulfx.control.NumberField;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import ru.shemplo.tbs.entity.Range;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;

public class RangeFields <N extends Number> extends HBox {
    
    private final NumberField <N> min, max;
    
    @SuppressWarnings ("unchecked")
    public RangeFields () {
        this ((Class <N>) Double.class);
    }
    
    public RangeFields (Class <N> type) {
        setAlignment (Pos.CENTER_LEFT);
        setSpacing (8.0);
        
        final var icon = new ImageView (TBSApplicationIcons.range);
        icon.setFitHeight (20);
        icon.setFitWidth (20);
        
        getChildren ().add (min = new NumberField <> (type));
        HBox.setHgrow (min, Priority.ALWAYS);
        //getChildren ().add (new Text ("< min | max >"));
        getChildren ().add (icon);
        getChildren ().add (max = new NumberField <> (type));
        HBox.setHgrow (max, Priority.ALWAYS);
    }
    
    public Range <N> getRange () {
        return new Range <> (min.getValue (), max.getValue ());
    }
    
    public void setMin (N value) {
        min.setValue (value);
    }
    
    public void setMax (N value) {
        max.setValue (value);
    }
    
}
