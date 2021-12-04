package ru.shemplo.tbs.gfx.launcher;

import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ru.shemplo.tbs.entity.IProfile;

public class TBSProfileCell extends ListCell <IProfile> {
    
    private final Text name, description;
    private final VBox root;
    
    public TBSProfileCell () {
        root = new VBox (4.0);
        
        root.getChildren ().add (name = new Text ());
        root.getChildren ().add (description = new Text ());
    }
    
    @Override
    protected void updateItem (IProfile item, boolean empty) {
        super.updateItem (item, empty);
        
        if (item != null) {
            description.setText (item.getProfileDescription ());
            name.setText (item.name ());
            
            setGraphic (root);
        } else {
            setGraphic (null);
            setText (null);
        }
    }
    
}
