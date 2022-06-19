package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.TBSConstants.*;
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
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.IBond;
import ru.shemplo.tbs.entity.ICoupon;
import ru.shemplo.tbs.entity.LinkedSymbolOrImage;
import ru.shemplo.tbs.gfx.table.TBSTableCell;

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
    
    public static TableView <ICoupon> initializeTable (IBond bond) {
        final var table = new TableView <ICoupon> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        table.getStylesheets ().setAll (STYLE_TABLES);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var grThreshold = TBSStyles.<ICoupon, Number> thresholdNotBefore (0.0, 1e-6, NOW, TBSCouponsInspection::fetchDate);
        
        final var profile = TBSUIApplication.getInstance ().getProfile ();
        final LocalDate now = bond.getNow (), end = bond.getEnd ();
        
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
        table.getColumns ().add (TBSUIUtils.<ICoupon, LocalDate> buildTBSTableColumn ()
            .name ("R date").tooltip ("Record date - list of bond owners is fixed after this date")
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (coup -> coup.getRWProperty ("record", null))
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
        table.getColumns ().add (TBSUIUtils.<ICoupon, Number> buildTBSTableColumn ()
            .name ("Credit").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (coup -> new MappingROProperty <> (
                coup.getRWProperty ("amount", null), 
                __ -> coup.getCredit (profile, now, end)
            ))
            .highlighter (grThreshold).converter (null)
            .build ());
        
        return table;
    }
    
    private static LocalDate fetchDate (TBSTableCell <ICoupon, ?> cell) {
        return TBSUtils.mapIfNN (cell.getTableRow ().getItem (), ICoupon::getDate, FAR_PAST);
    }
    
}
