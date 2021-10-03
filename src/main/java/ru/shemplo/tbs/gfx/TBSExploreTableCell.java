package ru.shemplo.tbs.gfx;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ru.shemplo.tbs.Bond;

public class TBSExploreTableCell extends TBSTableCell <Bond, Void> {

    private boolean openInTinkoff;
    
    public TBSExploreTableCell (boolean openInTinkoff) {
        super (__ -> null, false);
        this.openInTinkoff = openInTinkoff;
        
        setAlignment (Pos.CENTER);
    }
    
    @Override
    protected void updateItem (Bond item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            final var link = new Text ("🌐");
            link.setOnMouseClicked (me -> {
                if (me.getButton () == MouseButton.PRIMARY) {
                    if (openInTinkoff) {                        
                        TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                            "https://www.tinkoff.ru/invest/bonds/%s/", item.getCode ()
                        ));
                    } else {
                        TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                            "https://www.moex.com/ru/issue.aspx?code=%s&utm_source=www.moex.com", item.getCode ()
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