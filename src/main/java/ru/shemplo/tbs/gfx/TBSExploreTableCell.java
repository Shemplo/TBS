package ru.shemplo.tbs.gfx;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import ru.shemplo.tbs.Bond;

public class TBSExploreTableCell extends TBSTableCell <Bond, Void> {

    public TBSExploreTableCell () {
        super (__ -> null, false);
        
        setTextAlignment (TextAlignment.CENTER);
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
                    TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                        "https://www.tinkoff.ru/invest/bonds/%s/", item.getCode ()
                    ));
                }
            });
            link.setCursor (Cursor.HAND);
            link.setFill (Color.BLUE);
            setGraphic (link);
        }
    }
    
}
