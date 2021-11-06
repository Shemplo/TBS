package ru.shemplo.tbs.gfx.dep;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.gfx.TBSUIApplication;
import ru.shemplo.tbs.gfx.table.TBSTableCell;

public class TBSExploreTableCell extends TBSTableCell <Bond, String> {

    private boolean openInTinkoff;
    
    public TBSExploreTableCell (boolean openInTinkoff) {
        super ((__, ___) -> "", null);
        this.openInTinkoff = openInTinkoff;
        
        setAlignment (Pos.CENTER);
    }
    
    @Override
    protected void updateItem (String item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            final var link = new Text ("🌐");
            link.setOnMouseClicked (me -> {
                if (me.getButton () == MouseButton.PRIMARY) {
                    if (openInTinkoff) {                        
                        TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                            "https://www.tinkoff.ru/invest/bonds/%s/", item
                        ));
                    } else {
                        TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                            "https://www.moex.com/ru/issue.aspx?code=%s&utm_source=www.moex.com", item
                        ));
                    }
                }
            });
            
            link.setCursor (Cursor.HAND);
            link.setFill (Color.BLUE);
            setGraphic (link);
        }
    }
    
}
