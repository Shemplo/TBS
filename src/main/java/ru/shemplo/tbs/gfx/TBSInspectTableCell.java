package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.time.LocalDate;
import java.util.function.BiConsumer;
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
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.Coupon;

public class TBSInspectTableCell extends TBSTableCell <Bond, Void> {

    public TBSInspectTableCell () {
        super ((__, ___) -> null, null);
        
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
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Coupons inspection | %s (%s)", bond.getName (), bond.getCode ()));
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
        table.getStylesheets ().setAll (STYLE_TABLES);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var grThreshold = TBSStyles.<Coupon, Double> threshold (0.0, 1e-6);
        
        final var symbolColumn = makeTBSTableColumn ("", Coupon::getSymbol, false, Pos.BASELINE_CENTER, 50.0, null);
        table.getColumns ().add (symbolColumn);
        
        final var dateColumn = makeTBSTableColumn ("Date", Coupon::getDate, false, 100.0, null);
        table.getColumns ().add (dateColumn);
        
        final var amountColumn = makeTBSTableColumn ("Amount", Coupon::getAmount, false, 90.0, grThreshold);
        table.getColumns ().add (amountColumn);
        
        final var reliableColumn = makeTBSTableColumn ("Reliable?", Coupon::isReliable, false, 90.0, null);
        table.getColumns ().add (reliableColumn);
        
        final var profile = TBSUIApplication.getInstance ().getProfile ();
        final LocalDate now = bond.getNow (), end = bond.getEnd ();
        
        final var creditColumn = makeTBSTableColumn ("Credit", c -> c.getCredit (profile, now, end), false, 90.0, grThreshold);
        table.getColumns ().add (creditColumn);
        
        return table;
    }
    
    public static <T> TableColumn <Coupon, Coupon> makeTBSTableColumn (
        String name, Function <Coupon, T> converter, boolean sortable, double minWidth,
        BiConsumer <TBSTableCell <Coupon, T>, T> highlighter
    ) {
        return makeTBSTableColumn (name, converter, sortable, Pos.BASELINE_LEFT, minWidth, highlighter);
    }
    
    public static <T> TableColumn <Coupon, Coupon> makeTBSTableColumn (
        String name, Function <Coupon, T> converter, boolean sortable, 
        Pos textAlignment, double minWidth,
        BiConsumer <TBSTableCell <Coupon, T>, T> highlighter
    ) {
        final var column = new TableColumn <Coupon, Coupon> (name);
        column.setCellFactory (__ -> new TBSTableCell <> (
            (r, coupon) -> TBSUtils.mapIfNN (converter, c -> c.apply (coupon), null), 
            highlighter, textAlignment)
        );
        column.setCellValueFactory (cell -> {
            return new SimpleObjectProperty <> (cell.getValue ());
        });
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
}
