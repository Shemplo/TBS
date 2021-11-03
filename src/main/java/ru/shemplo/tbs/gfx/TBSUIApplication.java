package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.TBSConstants.*;
import static ru.shemplo.tbs.gfx.TBSStyles.*;
import static ru.shemplo.tbs.gfx.TBSUIUtils.*;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.ITBSProfile;

public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TableView <Bond> tableScanned, tablePortfolio;
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
    
    private TableView <Bond> initializeTable (TBSTableType type) {
        final var table = new TableView <Bond> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        table.getStylesheets ().setAll (STYLE_TABLES);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var exploreTinkoffColumn = new TableColumn <Bond, Bond> ("T");
        exploreTinkoffColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        exploreTinkoffColumn.setCellFactory (__ -> new TBSExploreTableCell (true));
        exploreTinkoffColumn.setSortable (false);
        exploreTinkoffColumn.setMinWidth (30);
        table.getColumns ().add (exploreTinkoffColumn);
        
        final var exploreMOEXColumn = new TableColumn <Bond, Bond> ("M");
        exploreMOEXColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        exploreMOEXColumn.setCellFactory (__ -> new TBSExploreTableCell (false));
        exploreMOEXColumn.setSortable (false);
        exploreMOEXColumn.setMinWidth (30);
        table.getColumns ().add (exploreMOEXColumn);
        
        final var inspectButtonColumn = new TableColumn <Bond, Bond> ("C");
        inspectButtonColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        inspectButtonColumn.setCellFactory (__ -> new TBSInspectTableCell ());
        inspectButtonColumn.setSortable (false);
        inspectButtonColumn.setMinWidth (30);
        table.getColumns ().add (inspectButtonColumn);
        
        final var plannerPinColumn = new TableColumn <Bond, Bond> ("📎");
        plannerPinColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        plannerPinColumn.setCellFactory (__ -> new TBSToggleTableCell <> (
            TBSPlanner.getInstance ().getBonds (),
            bond -> {
                final var planner = TBSPlanner.getInstance ();
                return planner.hasBond (bond.getCode ());
            }, (bond, selected) -> {
                final var planner = TBSPlanner.getInstance ();
                if (selected) {
                    planner.addBond (bond.getCode (), bond.getScore (), bond.getLastPrice ());
                } else {
                    planner.removeBond (bond.getCode ());
                }
            }
        ));
        plannerPinColumn.setSortable (false);
        plannerPinColumn.setMinWidth (30);
        table.getColumns ().add (plannerPinColumn);
        
        final var grThreshold = TBSStyles.<Bond, Number> threshold (0.0, 1e-6);
        final var fixedCoupons = TBSStyles.<Bond> fixedCoupons ();
        final var sameMonth = TBSStyles.<Bond> sameMonth (NOW);
        
        final var shortNameColumn = makeTBSTableColumn (
            "Bond name", null, Bond::getName, false, 
            250.0, Pos.BASELINE_LEFT, null
        );
        table.getColumns ().add (shortNameColumn);
        
        final var codeColumn = makeTBSTableColumn (
            "Code", "Bond ticker", Bond::getCode, false, 
            125.0, Pos.BASELINE_LEFT, null
        );
        table.getColumns ().add (codeColumn);
        
        final var currencyColumn = makeTBSTableColumn (
            "Currency", null, Bond::getCurrency, false, 
            80.0, Pos.BASELINE_LEFT, null
        );
        table.getColumns ().add (currencyColumn);
        
        final var lotsColumn = makeTBSTableColumn (
            "👝", "Number of lots in your portfolio (sum by all your accounts)", 
            Bond::getLots, false, 50.0, Pos.BASELINE_LEFT, grThreshold
        );
        table.getColumns ().add (lotsColumn);
        
        if (type == TBSTableType.SCANNED) {
            final var scoreColumn = makeTBSTableColumn (
                "Score", null, Bond::getScore, false, 80.0, 
                Pos.BASELINE_LEFT, grThreshold
            );
            table.getColumns ().add (scoreColumn);
        }
        
        if (type == TBSTableType.SCANNED) {
            final var pureCreditColumn = makeTBSTableColumn (
                "Credit", "Coupons credit plus difference between price and inflated price", 
                Bond::getPureCredit, false, 80.0, Pos.BASELINE_LEFT, grThreshold
            );
            table.getColumns ().add (pureCreditColumn);
        }
        
        final var couponsCreditColumn = makeTBSTableColumn (
            "Coupons", "Sum of coupons since the next coupon date with inflation", 
            Bond::getCouponsCredit, false, 80.0, Pos.BASELINE_LEFT, grThreshold
        );
        table.getColumns ().add (couponsCreditColumn);
        
        if (type == TBSTableType.SCANNED) {       
            final var priceColumn = makeTBSTableColumn (
                "Price", "Last commited price in MOEX",
                Bond::getLastPrice, false, 80.0, Pos.BASELINE_LEFT, grThreshold
            );
            table.getColumns ().add (priceColumn);
        }
        
        final var nominalColumn = makeTBSTableColumn (
            "Nominal", null, Bond::getNominalValue, false, 
            80.0, Pos.BASELINE_LEFT, null
        );
        table.getColumns ().add (nominalColumn);
        
        final var couponsPerYearColumn = makeTBSTableColumn (
            "C / Y", "Coupons per year", 
            Bond::getCouponsPerYear, false, 50.0, Pos.BASELINE_LEFT, null
        );
        table.getColumns ().add (couponsPerYearColumn);
        
        final var nextCouponColumn = makeTBSTableColumn (
            "Next C", "Closest date of the next coupon", 
            Bond::getNextCoupon, false, 90.0, Pos.BASELINE_LEFT, sameMonth
        );
        table.getColumns ().add (nextCouponColumn);
        
        final var couponFixedColumn = makeTBSTableColumn (
            "C mode", "Coupon mode",
            Bond::getCouponValuesMode, false, 90.0, Pos.BASELINE_LEFT, fixedCoupons
        );
        table.getColumns ().add (couponFixedColumn);
        
        if (type == TBSTableType.SCANNED) {            
            final var yearsColumn = makeTBSTableColumn (
                "Ys", "Years till end", 
                Bond::getYearsToEnd, false, 50.0, Pos.BASELINE_LEFT, null
            );
            table.getColumns ().add (yearsColumn);
            
            final var monthsColumn = TBSUIUtils.<Bond, Long> makeTBSTableColumn (
                "Ms", "Months till end (value from range 0 to 12)", 
                bnd -> bnd.getMonthToEnd () % 12, false, 50.0, Pos.BASELINE_LEFT, null
            );
            table.getColumns ().add (monthsColumn);
        }
        
        final var percentageColumn = makeTBSTableColumn ("MOEX %", null, Bond::getPercentage, false, 60.0, Pos.BASELINE_LEFT, grThreshold);
        table.getColumns ().add (percentageColumn);
        
        return table;
    }
    
    public void applyData (ITBSProfile profile) {
        this.profile = profile;
        
        profileDetails.setText (profile.getProfileDescription ());
        
        final var bondManager = TBSBondManager.getInstance ();
        tablePortfolio.setItems (FXCollections.observableArrayList (bondManager.getPortfolio ()));
        tableScanned.setItems (FXCollections.observableArrayList (bondManager.getScanned ()));
        
        plannerTool.refreshData (profile);
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
