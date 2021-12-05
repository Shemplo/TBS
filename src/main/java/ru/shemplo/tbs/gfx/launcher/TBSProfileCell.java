package ru.shemplo.tbs.gfx.launcher;

import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.gfx.TBSStyles;

public class TBSProfileCell extends ListCell <IProfile> {
    
    private final Text name, description;
    private final VBox root;
    
    public TBSProfileCell () {
        setPadding (Insets.EMPTY);
        
        root = new VBox (4.0);
        root.setPadding (new Insets (4.0));
        
        root.getChildren ().add (name = new Text ());
        name.setFont (TBSStyles.FONT_BOLD_14);
        
        root.getChildren ().add (description = new Text ());
        description.setFont (TBSStyles.FONT_MONO_12);
    }
    
    @Override
    protected void updateItem (IProfile item, boolean empty) {
        super.updateItem (item, empty);
        
        if (item != null) {
            description.setText (item.getShortProfileDescription ());
            name.setText (item.name ());
            
            setGraphic (root);
        } else {
            setGraphic (null);
            setText (null);
        }
    }
    
    @Override
    public void updateSelected (boolean selected) {
        super.updateSelected (selected);
        
        description.setFill (selected ? Color.WHITE : Color.BLACK);
        name.setFill (description.getFill ());
    }
    
}
