package ru.shemplo.tbs.gfx;

import java.util.List;
import java.util.function.Function;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
import ru.shemplo.tbs.TBSUtils;

public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TableView <TBSMetaWrapper <Bond>> table;
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
        
        root.getChildren ().add (table = initializeTable ());
        instance = this;
    }
    
    private TableView <TBSMetaWrapper <Bond>> initializeTable () {
        final var table = new TableView <TBSMetaWrapper <Bond>> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var exploreTinkoffColumn = new TableColumn <TBSMetaWrapper <Bond>, TBSMetaWrapper <Bond>> ("T");
        exploreTinkoffColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        exploreTinkoffColumn.setCellFactory (__ -> new TBSExploreTableCell (true));
        exploreTinkoffColumn.setMinWidth (30);
        table.getColumns ().add (exploreTinkoffColumn);
        
        final var exploreMOEXColumn = new TableColumn <TBSMetaWrapper <Bond>, TBSMetaWrapper <Bond>> ("M");
        exploreMOEXColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        exploreMOEXColumn.setCellFactory (__ -> new TBSExploreTableCell (false));
        exploreMOEXColumn.setMinWidth (30);
        table.getColumns ().add (exploreMOEXColumn);
        
        final var ispectButtonColumn = new TableColumn <TBSMetaWrapper <Bond>, TBSMetaWrapper <Bond>> ("C");
        ispectButtonColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        ispectButtonColumn.setCellFactory (__ -> new TBSInspectTableCell ());
        ispectButtonColumn.setMinWidth (30);
        table.getColumns ().add (ispectButtonColumn);
        
        final var shortNameColumn = makeTBSTableColumn ("Bond name", Bond::getName, false, false, 250.0);
        table.getColumns ().add (shortNameColumn);
        
        final var codeColumn = makeTBSTableColumn ("Code", Bond::getCode, false, false, 125.0);
        table.getColumns ().add (codeColumn);
        
        final var currencyColumn = makeTBSTableColumn ("Currency", Bond::getCurrency, false, false, 90.0);
        table.getColumns ().add (currencyColumn);
        
        final var lotsColumn = makeTBSTableColumn ("👝", Bond::getLots, false, true, 30.0);
        table.getColumns ().add (lotsColumn);
        
        final var scoreColumn = makeTBSTableColumn ("Score", Bond::getScore, false, true, 90.0);
        table.getColumns ().add (scoreColumn);
        
        final var pureCreditColumn = makeTBSTableColumn ("Pure credit", Bond::getPureCredit, false, true, 90.0);
        table.getColumns ().add (pureCreditColumn);
        
        final var couponsCreditColumn = makeTBSTableColumn ("Coupons", Bond::getCouponsCredit, false, true, 90.0);
        table.getColumns ().add (couponsCreditColumn);
        
        final var priceColumn = makeTBSTableColumn ("Price", Bond::getLastPrice, false, true, 90.0);
        table.getColumns ().add (priceColumn);
        
        final var nominalColumn = makeTBSTableColumn ("Nominal", Bond::getNominalValue, false, false, 90.0);
        table.getColumns ().add (nominalColumn);
        
        final var couponsPerYearColumn = makeTBSTableColumn ("C / Y", Bond::getCouponsPerYear, false, false, 50.0);
        table.getColumns ().add (couponsPerYearColumn);
        
        final var nextCouponColumn = makeTBSTableColumn ("Next coupon", Bond::getNextCoupon, false, false, 100.0);
        table.getColumns ().add (nextCouponColumn);
        
        final var couponFixedColumn = makeTBSTableColumn ("C mode", Bond::getCouponValuesMode, false, false, 100.0);
        table.getColumns ().add (couponFixedColumn);
        
        final var yearsColumn = makeTBSTableColumn ("Years", Bond::getYearsToEnd, false, false, 50.0);
        table.getColumns ().add (yearsColumn);
        
        final var monthsColumn = makeTBSTableColumn ("Months", bnd -> bnd.getMonthToEnd () % 12, false, false, 50.0);
        table.getColumns ().add (monthsColumn);
        
        final var percentageColumn = makeTBSTableColumn ("MOEX %", Bond::getPercentage, false, true, 50.0);
        table.getColumns ().add (percentageColumn);
        
        return table;
    }
    
    public static <T> TableColumn <TBSMetaWrapper <Bond>, TBSMetaWrapper <Bond>> makeTBSTableColumn (
        String name, Function <Bond, T> converter, boolean sortable, boolean colorized, double minWidth
    ) {
        final var column = new TableColumn <TBSMetaWrapper <Bond>, TBSMetaWrapper <Bond>> (name);
        column.setCellFactory (__ -> new TBSTableCell <> (converter, colorized));
        column.setCellValueFactory (cell -> {
            return new SimpleObjectProperty <> (cell.getValue ());
        });
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
    public void applyData (ITBSProfile profile, List <Bond> bonds) {
        profileDetails.setText (profile.getProfileDescription ());
        table.setItems (FXCollections.observableArrayList (
            TBSUtils.mapToList (bonds, TBSMetaWrapper::new)
        ));
        this.profile = profile;
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
