package ru.shemplo.tbs.gfx;

import java.time.LocalDate;
import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.Bond;
import ru.shemplo.tbs.Coupon;

public class TBSInspectTableCell extends TBSTableCell <Bond, Void> {

    public TBSInspectTableCell () {
        super (__ -> null, false);
        
        setTextAlignment (TextAlignment.CENTER);
        setAlignment (Pos.CENTER);
    }
    
    @Override
    protected void updateItem (Bond item, boolean empty) {
        super.updateItem (item, empty);
        setText (null);
        
        if (item != null) {
            final var link = new Text ("🔍");
            link.setOnMouseClicked (me -> {
                if (me.getButton () == MouseButton.PRIMARY) {
                    final var scene = ((Node) me.getSource ()).getScene ();
                    showCouponsWindow (scene.getWindow (), item);
                }
            });
            link.setCursor (Cursor.HAND);
            link.setFill (Color.BLUE);
            setGraphic (link);
        }
    }
    
    private void showCouponsWindow (Window window, Bond bond) {
        final var stage = new Stage ();
        stage.setTitle ("Tinkoff Bonds Scanner | Coupons inspection");
        stage.initModality (Modality.WINDOW_MODAL);
        stage.initOwner (window);
        
        final var root = new VBox ();
        final var scene = new Scene (root);
        
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.setScene (scene);
        stage.show ();
        
        final var table = initializeTable (bond);
        root.getChildren ().add (table);
        
        table.setItems (FXCollections.observableArrayList (bond.getCoupons ()));
    }
    
    private TableView <Coupon> initializeTable (Bond bond) {
        final var table = new TableView <Coupon> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var symbolColumn = makeTBSTableColumn ("", Coupon::getSymbol, false, false, Pos.BASELINE_CENTER, 30.0);
        table.getColumns ().add (symbolColumn);
        
        final var dateColumn = makeTBSTableColumn ("Date", Coupon::getDate, false, false, 100.0);
        table.getColumns ().add (dateColumn);
        
        final var amountColumn = makeTBSTableColumn ("Amount", Coupon::getAmount, false, true, 90.0);
        table.getColumns ().add (amountColumn);
        
        final var reliableColumn = makeTBSTableColumn ("Reliable?", Coupon::isReliable, false, true, 90.0);
        table.getColumns ().add (reliableColumn);
        
        final var profile = TBSUIApplication.getInstance ().getProfile ();
        final LocalDate now = LocalDate.now (), end = bond.getEnd ();
        
        final var creditColumn = makeTBSTableColumn ("Credit", c -> c.getCredit (profile, now, end), true, true, 90.0);
        table.getColumns ().add (creditColumn);
        
        return table;
    }
    
    public static <T> TableColumn <Coupon, Coupon> makeTBSTableColumn (
        String name, Function <Coupon, T> converter, boolean sortable, boolean colorized, double minWidth
    ) {
        return makeTBSTableColumn (name, converter, sortable, colorized, Pos.BASELINE_LEFT, minWidth);
    }
    
    public static <T> TableColumn <Coupon, Coupon> makeTBSTableColumn (
        String name, Function <Coupon, T> converter, boolean sortable, boolean colorized, 
        Pos textAlignment, double minWidth
    ) {
        final var column = new TableColumn <Coupon, Coupon> (name);
        column.setCellFactory (__ -> new TBSTableCell <> (converter, colorized, textAlignment));
        column.setCellValueFactory (cell -> {
            return new SimpleObjectProperty <> (cell.getValue ());
        });
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
}
