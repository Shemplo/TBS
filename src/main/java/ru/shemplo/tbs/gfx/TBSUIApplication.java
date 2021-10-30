package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.TBSConstants.*;
import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
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
import ru.shemplo.tbs.Bond;
import ru.shemplo.tbs.ITBSProfile;

public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TableView <Bond> tableScanned, tablePortfolio;
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
        tabScanned.setContent (tableScanned = initializeTable (TBSTableType.SCANNED));
        tabScanned.setClosable (false);
        tabs.getTabs ().add (tabScanned);
        
        final var tabPortfolio = new Tab ("Portfolio");
        tabPortfolio.setContent (tablePortfolio = initializeTable (TBSTableType.PORTFOLIO));
        tabPortfolio.setClosable (false);
        tabs.getTabs ().add (tabPortfolio);
        
        //root.getChildren ().add (table = initializeTable ());
        instance = this;
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
        exploreTinkoffColumn.setMinWidth (30);
        table.getColumns ().add (exploreTinkoffColumn);
        
        final var exploreMOEXColumn = new TableColumn <Bond, Bond> ("M");
        exploreMOEXColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        exploreMOEXColumn.setCellFactory (__ -> new TBSExploreTableCell (false));
        exploreMOEXColumn.setMinWidth (30);
        table.getColumns ().add (exploreMOEXColumn);
        
        final var ispectButtonColumn = new TableColumn <Bond, Bond> ("C");
        ispectButtonColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        ispectButtonColumn.setCellFactory (__ -> new TBSInspectTableCell ());
        ispectButtonColumn.setMinWidth (30);
        table.getColumns ().add (ispectButtonColumn);
        
        final var grThreshold = TBSStyles.<Bond, Number> threshold (0.0, 1e-6);
        final var fixedCoupons = TBSStyles.<Bond> fixedCoupons ();
        final var sameMonth = TBSStyles.<Bond> sameMonth (NOW);
        
        final var shortNameColumn = makeTBSTableColumn ("Bond name", Bond::getName, false, 250.0, null);
        table.getColumns ().add (shortNameColumn);
        
        final var codeColumn = makeTBSTableColumn ("Code", Bond::getCode, false, 125.0, null);
        table.getColumns ().add (codeColumn);
        
        final var currencyColumn = makeTBSTableColumn ("Currency", Bond::getCurrency, false, 90.0, null);
        table.getColumns ().add (currencyColumn);
        
        final var lotsColumn = makeTBSTableColumn ("👝", Bond::getLots, false, 50.0, grThreshold);
        table.getColumns ().add (lotsColumn);
        
        if (type == TBSTableType.SCANNED) {
            final var scoreColumn = makeTBSTableColumn ("Score", Bond::getScore, false, 90.0, grThreshold);
            table.getColumns ().add (scoreColumn);
        }
        
        if (type == TBSTableType.SCANNED) {
            final var pureCreditColumn = makeTBSTableColumn ("Pure credit", Bond::getPureCredit, false, 90.0, grThreshold);
            table.getColumns ().add (pureCreditColumn);
        }
        
        final var couponsCreditColumn = makeTBSTableColumn ("Coupons", Bond::getCouponsCredit, false, 90.0, grThreshold);
        table.getColumns ().add (couponsCreditColumn);
        
        if (type == TBSTableType.SCANNED) {       
            final var priceColumn = makeTBSTableColumn ("Price", Bond::getLastPrice, false, 90.0, grThreshold);
            table.getColumns ().add (priceColumn);
        }
        
        final var nominalColumn = makeTBSTableColumn ("Nominal", Bond::getNominalValue, false, 90.0, null);
        table.getColumns ().add (nominalColumn);
        
        final var couponsPerYearColumn = makeTBSTableColumn ("C / Y", Bond::getCouponsPerYear, false, 50.0, null);
        table.getColumns ().add (couponsPerYearColumn);
        
        final var nextCouponColumn = makeTBSTableColumn ("Next coupon", Bond::getNextCoupon, false, 100.0, sameMonth);
        table.getColumns ().add (nextCouponColumn);
        
        final var couponFixedColumn = makeTBSTableColumn ("C mode", Bond::getCouponValuesMode, false, 100.0, fixedCoupons);
        table.getColumns ().add (couponFixedColumn);
        
        if (type == TBSTableType.SCANNED) {            
            final var yearsColumn = makeTBSTableColumn ("Years", Bond::getYearsToEnd, false, 50.0, null);
            table.getColumns ().add (yearsColumn);
            
            final var monthsColumn = makeTBSTableColumn ("Months", bnd -> bnd.getMonthToEnd () % 12, false, 50.0, null);
            table.getColumns ().add (monthsColumn);
        }
        
        final var percentageColumn = makeTBSTableColumn ("MOEX %", Bond::getPercentage, false, 50.0, grThreshold);
        table.getColumns ().add (percentageColumn);
        
        return table;
    }
    
    public static <T> TableColumn <Bond, Bond> makeTBSTableColumn (
        String name, Function <Bond, T> converter, boolean sortable, double minWidth,
        BiConsumer <TBSTableCell <Bond, T>, T> highlighter
    ) {
        final var column = new TableColumn <Bond, Bond> (name);
        column.setCellFactory (__ -> new TBSTableCell <> (converter, highlighter));
        column.setCellValueFactory (cell -> {
            return new SimpleObjectProperty <> (cell.getValue ());
        });
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
    public void applyData (ITBSProfile profile, List <Bond> bonds, List <Bond> portfolio) {
        profileDetails.setText (profile.getProfileDescription ());
        tablePortfolio.setItems (FXCollections.observableArrayList (portfolio));
        tableScanned.setItems (FXCollections.observableArrayList (bonds));
        this.profile = profile;
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
