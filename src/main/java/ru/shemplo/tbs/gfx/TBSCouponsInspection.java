package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.MappingROProperty;
import ru.shemplo.tbs.entity.IBond;
import ru.shemplo.tbs.entity.ICoupon;

public class TBSCouponsInspection {
    
    public TBSCouponsInspection (Window window, IBond bond) {
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
    
    private TableView <ICoupon> initializeTable (IBond bond) {
        final var table = new TableView <ICoupon> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        table.getStylesheets ().setAll (STYLE_TABLES);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var grThreshold = TBSStyles.<ICoupon, Number> threshold (0.0, 1e-6);
        //final var sameMonth = TBSStyles.<ICoupon> sameMonth (NOW);
        
        table.getColumns ().add (TBSUIUtils.<ICoupon, LinkedSymbolOrImage> buildTBSIconTableColumn ()
            .name ("").tooltip (null).minWidth (50.0).sortable (false)
            .propertyFetcher (coup -> new MappingROProperty <> (
                coup.getRWProperty ("symbol", () -> ""), 
                v -> LinkedSymbolOrImage.symbol (v, bond.getCode ())
            ))
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<ICoupon, LocalDate> buildTBSTableColumn ()
            .name ("Date").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (coup -> coup.getRWProperty ("date", null))
            .highlighter (null).converter ((c, v) -> String.valueOf (v))
            .build ());
        table.getColumns ().add (TBSUIUtils.<ICoupon, Number> buildTBSTableColumn ()
            .name ("Amount").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (coup -> coup.getRWProperty ("amount", null))
            .highlighter (grThreshold).converter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<ICoupon, Boolean> buildTBSTableColumn ()
            .name ("Reliable?").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (coup -> coup.getRWProperty ("reliable", null))
            .highlighter (null).converter ((__, v) -> String.valueOf (v))
            .build ());
        
        /*
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
        */
        
        return table;
    }
    
}
