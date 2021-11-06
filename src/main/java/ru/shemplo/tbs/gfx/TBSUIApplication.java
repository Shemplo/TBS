package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.TBSConstants.*;
import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.time.LocalDate;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSPlanner;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.CouponValueMode;
import ru.shemplo.tbs.entity.IBond;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.shemplo.tbs.gfx.table.TBSTableCell;
import ru.tinkoff.invest.openapi.model.rest.Currency;

public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TableView <IBond> tableScanned, tablePortfolio;
    private TBBSPlannerTool plannerTool;
    private Text profileDetails;
    
    @Getter
    private Stage stage;
    
    @Getter
    private ITBSProfile profile;
    
    @Override
    public void start (Stage stage) throws Exception {
        this.stage = stage;
        
        final var root = new VBox ();
        final var scene = new Scene (root);
        
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.setTitle ("Tinkoff Bonds Scanner | v1.0");
        stage.setMaximized (true);
        stage.setScene (scene);
        stage.show ();
        
        root.getChildren ().add (profileDetails = new Text ());
        profileDetails.setFont (Font.font ("Consolas", 10.0));
        VBox.setMargin (profileDetails, new Insets (12.0));
        
        final var tabs = new TabPane ();
        tabs.getStylesheets ().add (STYLE_TABS);
        VBox.setVgrow (tabs, Priority.ALWAYS);
        root.getChildren ().add (tabs);
        
        final var tabScanned = new Tab ("Scanned bonds");
        tabScanned.setContent (makeTabContent (tableScanned = initializeTable (TBSTableType.SCANNED)));
        tabScanned.setOnSelectionChanged (e -> {
            //tableScanned.refresh ();
        });
        tabScanned.setClosable (false);
        tabs.getTabs ().add (tabScanned);
        
        final var tabPortfolio = new Tab ("Portfolio bonds");
        tabPortfolio.setContent (makeTabContent (tablePortfolio = initializeTable (TBSTableType.PORTFOLIO)));
        tabPortfolio.setOnSelectionChanged (e -> {
            //tablePortfolio.refresh ();
        });
        tabPortfolio.setClosable (false);
        tabs.getTabs ().add (tabPortfolio);
        
        final var tabPlanner = new Tab ("Planning tool");
        tabPlanner.setContent (plannerTool = new TBBSPlannerTool ());
        tabPlanner.setClosable (false);
        tabs.getTabs ().add (tabPlanner);
        
        instance = this;
    }
    
    private Parent makeTabContent (Node content) {
        final var wrapper = new VBox ();
        wrapper.setPadding (new Insets (2, 0, 0, 0));
        wrapper.getChildren ().add (content);
        wrapper.setFillWidth (true);
        return wrapper;
    }
    
    private TableView <IBond> initializeTable (TBSTableType type) {
        final var table = new TableView <IBond> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        table.getStylesheets ().setAll (STYLE_TABLES);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var grThreshold = TBSStyles.<IBond, Number> threshold (0.0, 1e-6);
        final var fixedCoupons = TBSStyles.<IBond> fixedCoupons ();
        final var sameMonth = TBSStyles.<IBond> sameMonth (NOW);
        final var linkIcon = TBSStyles.<IBond> linkIcon ();
        
        table.getColumns ().add (TBSUIUtils.<IBond, LinkedSymbolOrImage> buildTBSIconTableColumn ()
            .name ("T").tooltip (null).minWidth (30.0).sortable (false)
            .propertyFetcher (b -> makeExloreProperty (b, "🌐")).highlighter (linkIcon)
            .onClick ((me, cell) -> handleExploreBrowserColumnClick (me, cell, true))
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, LinkedSymbolOrImage> buildTBSIconTableColumn ()
            .name ("M").tooltip (null).minWidth (30.0).sortable (false)
            .propertyFetcher (b -> makeExloreProperty (b, "🌐")).highlighter (linkIcon)
            .onClick ((me, cell) -> handleExploreBrowserColumnClick (me, cell, true))
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, LinkedSymbolOrImage> buildTBSIconTableColumn ()
            .name ("C").tooltip (null).minWidth (30.0).sortable (false)
            .propertyFetcher (b -> makeExloreProperty (b, "🔍")).highlighter (linkIcon)
            .onClick ((me, cell) -> handleExploreCouponsColumnClick (me, cell))
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, LinkedObject <Boolean>> buildTBSToggleTableColumn ()
            .name ("📎").tooltip (null).minWidth (30.0).sortable (false)
            .propertyFetcher (this::makePinProperty).highlighter (null)
            .onToggle (this::handlePlannerPinToggle)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, String> buildTBSTableColumn ()
            .name ("Name").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (250.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("name", () -> "")).converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, String> buildTBSTableColumn ()
            .name ("Ticker").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (125.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("code", () -> "")).converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, Currency> buildTBSTableColumn ()
            .name ("Currency").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("currency", null))
            .converter ((r, v) -> String.valueOf (v)).highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
            .name ("👝").tooltip ("Number of lots in your portfolio (sum by all your accounts)")
            .alignment (Pos.BASELINE_LEFT).minWidth (50.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("lots", null))
            .highlighter (grThreshold).converter (null)
            .build ());
        if (type == TBSTableType.SCANNED) {
            table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
                .name ("Score").tooltip (null)
                .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
                .propertyFetcher (bond -> bond.getRWProperty ("score", null))
                .highlighter (grThreshold).converter (null)
                .build ());
            table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
                .name ("Credit").tooltip ("Coupons credit plus difference between price and inflated price")
                .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
                .propertyFetcher (bond -> bond.getRWProperty ("pureCredit", null))
                .highlighter (grThreshold).converter (null)
                .build ());
        }   
        table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
            .name ("Coupons").tooltip ("Sum of coupons since the next coupon date with inflation")
            .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("couponsCredit", null))
            .highlighter (grThreshold).converter (null)
            .build ());
        if (type == TBSTableType.SCANNED) {
            table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
                .name ("Price").tooltip ("Last committed price in MOEX")
                .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
                .propertyFetcher (bond -> bond.getRWProperty ("lastPrice", null))
                .highlighter (grThreshold).converter (null)
                .build ());
        }
        table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
            .name ("Nominal").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("nominalValue", null))
            .highlighter (null).converter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
            .name ("C / Y").tooltip ("Coupons per year")
            .alignment (Pos.BASELINE_LEFT).minWidth (50.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("couponsPerYear", null))
            .highlighter (null).converter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, LocalDate> buildTBSTableColumn ()
            .name ("Next C").tooltip ("Closest date of the next coupon")
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("nextCoupon", null))
            .highlighter (sameMonth).converter ((c, v) -> String.valueOf (v))
            .build ());
        table.getColumns ().add (TBSUIUtils.<IBond, CouponValueMode> buildTBSTableColumn ()
            .name ("C mode").tooltip ("Coupon mode")
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (bond -> new SimpleObjectProperty <> (bond.getCouponValuesMode ()))
            .converter ((c, v) -> TBSUtils.mapIfNN (v, CouponValueMode::name, ""))
            .highlighter (fixedCoupons)
            .build ());
        if (type == TBSTableType.SCANNED) {
            table.getColumns ().add (TBSUIUtils.<IBond, Long> buildTBSTableColumn ()
                .name ("Ys").tooltip ("Years till end")
                .alignment (Pos.BASELINE_LEFT).minWidth (50.0).sortable (false)
                .propertyFetcher (bond -> new SimpleObjectProperty <> (bond.getYearsToEnd ()))
                .highlighter (null).converter (null)
                .build ());
            table.getColumns ().add (TBSUIUtils.<IBond, Long> buildTBSTableColumn ()
                .name ("Ys").tooltip ("Months till end (value from range 0 to 12)")
                .alignment (Pos.BASELINE_LEFT).minWidth (50.0).sortable (false)
                .propertyFetcher (bond -> new SimpleObjectProperty <> (bond.getMonthsToEnd () % 12))
                .highlighter (null).converter (null)
                .build ());
        }
        table.getColumns ().add (TBSUIUtils.<IBond, Number> buildTBSTableColumn ()
            .name ("MOEX %").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (60.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("percentage", null))
            .highlighter (grThreshold).converter (null)
            .build ());
        
        return table;
    }
    
    private ObjectProperty <LinkedObject <Boolean>> makePinProperty (IBond bond) {
        final var planner = TBSPlanner.getInstance ();
        
        final var codePropery = bond.getRWProperty ("code", () -> "");
        final var plannerBondsProperty = planner.getBonds ();
        final var codeValue = codePropery.get ();
        
        final var UIProperty = bond.getProperty (IBond.UI_SELECTED_PROPERTY, 
            () -> new LinkedObject <> (codeValue, planner.hasBond (codeValue)), false
        );
        UIProperty.bind (Bindings.createObjectBinding (
            () -> new LinkedObject <> (codePropery.get (), planner.hasBond (codePropery.get ())), 
            codePropery, plannerBondsProperty
        ));
        
        return UIProperty;
    }
    
    private void handlePlannerPinToggle (LinkedObject <Boolean> item, Boolean selected) {
        TBSUtils.doIfNN (item, i -> {
            if (TBSUtils.aOrB (selected, false)) {
                TBSPlanner.getInstance ().addBond (i.getLink ());
            } else {
                TBSPlanner.getInstance ().removeBond (i.getLink ());
            }
        });
    }
    
    private ObjectProperty <LinkedSymbolOrImage> makeExloreProperty (IBond bond, String symbol) {
        final var codePropery = bond.getRWProperty ("code", () -> "");
        final var property = new SimpleObjectProperty <LinkedSymbolOrImage> ();
        property.bind (Bindings.createObjectBinding (
            () -> LinkedSymbolOrImage.symbol (symbol, codePropery.get ()), 
            codePropery
        ));
        return property;
    }
    
    private void handleExploreBrowserColumnClick (MouseEvent me, TBSTableCell <IBond, LinkedSymbolOrImage> cell, boolean openInTinkoff) {
        if (me.getButton () == MouseButton.PRIMARY) {
            if (openInTinkoff) {                        
                TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                    "https://www.tinkoff.ru/invest/bonds/%s/", cell.getItem ().getLink ()
                ));
            } else {
                TBSUIApplication.getInstance ().openLinkInBrowser (String.format (
                    "https://www.moex.com/ru/issue.aspx?code=%s&utm_source=www.moex.com", 
                    cell.getItem ().getLink ()
                ));
            }
        }
    }
    
    private void handleExploreCouponsColumnClick (MouseEvent me, TBSTableCell <IBond, LinkedSymbolOrImage> cell) {
        if (me.getButton () == MouseButton.PRIMARY && cell.getItem () != null) {
            final var bond = TBSBondManager.getBondByTicker (cell.getItem ().getLink (), true);
            final var scene = ((Node) me.getSource ()).getScene ();
            new TBSCouponsInspection (scene.getWindow (), bond);
        }
    }
    
    public void applyData (ITBSProfile profile) {
        this.profile = profile;
        
        profileDetails.setText (profile.getProfileDescription ());
        
        final var bondManager = TBSBondManager.getInstance ();
        tablePortfolio.setItems (FXCollections.observableArrayList (
            bondManager.getPortfolio ().stream ().map (Bond::getProxy).toList ()
        ));
        tableScanned.setItems (FXCollections.observableArrayList (
            bondManager.getScanned ().stream ().map (Bond::getProxy).toList ()
        ));
        
        plannerTool.applyData (profile);
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
