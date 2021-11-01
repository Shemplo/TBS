package ru.shemplo.tbs.gfx;

import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import ru.shemplo.tbs.entity.ITBSProfile;

public class TBBSPlannerTool extends HBox {
    
    public TBBSPlannerTool () {
        setFillHeight (true);
        
        makeLeftPannel ();
        makeTable ();
    }
    
    private Parent makeLeftPannel () {
        return null;
    }
    
    private TableView <?> makeTable () {
        return null;
    }
    
    public void refreshData (ITBSProfile profile) {
        
    }
    
}
