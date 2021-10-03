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
import ru.shemplo.tbs.TBSProfile;

public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TableView <Bond> table;
    private Text profileDetails;
    
    @Getter
    private Stage stage;
    
    @Getter
    private TBSProfile profile;
    
    @Override
    public void start (Stage stage) throws Exception {
        this.stage = stage;
        
        final var root = new VBox ();
        final var scene = new Scene (root);
        
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.setTitle ("Tinkoff Bonds Scanner | v0.0.1");
        stage.setMaximized (true);
        stage.setScene (scene);
        stage.show ();
        
        root.getChildren ().add (profileDetails = new Text ());
        VBox.setMargin (profileDetails, new Insets (8.0));
        profileDetails.setFont (Font.font ("Consolas"));
        
        root.getChildren ().add (table = initializeTable ());
        instance = this;
    }
    
    private TableView <Bond> initializeTable () {
        final var table = new TableView <Bond> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var exploreButtonColumn = new TableColumn <Bond, Bond> ();
        exploreButtonColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        exploreButtonColumn.setCellFactory (__ -> new TBSExploreTableCell ());
        exploreButtonColumn.setMinWidth (30);
        table.getColumns ().add (exploreButtonColumn);
        
        final var ispectButtonColumn = new TableColumn <Bond, Bond> ();
        ispectButtonColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        ispectButtonColumn.setCellFactory (__ -> new TBSInspectTableCell ());
        ispectButtonColumn.setMinWidth (30);
        table.getColumns ().add (ispectButtonColumn);
        
        final var shortNameColumn = makeTBSTableColumn ("Bond name", Bond::getName, false, false, 300.0);
        table.getColumns ().add (shortNameColumn);
        
        final var codeColumn = makeTBSTableColumn ("Code", Bond::getCode, false, false, 125.0);
        table.getColumns ().add (codeColumn);
        
        final var currencyColumn = makeTBSTableColumn ("Currency", Bond::getCurrency, false, false, 90.0);
        table.getColumns ().add (currencyColumn);
        
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
    
    public static <T> TableColumn <Bond, Bond> makeTBSTableColumn (
        String name, Function <Bond, T> converter, boolean sortable, boolean colorized, double minWidth
    ) {
        final var column = new TableColumn <Bond, Bond> (name);
        column.setCellFactory (__ -> new TBSTableCell <> (converter, colorized));
        column.setCellValueFactory (cell -> {
            /*
            final var value = converter.apply (cell.getValue ());
            if (value instanceof Double d) {
                return new SimpleStringProperty (String.format ("%.1f", d));
            } else {
                return new SimpleStringProperty (String.valueOf (value));
            }
            */
            return new SimpleObjectProperty <> (cell.getValue ());
        });
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
    public void applyData (TBSProfile profile, List <Bond> bonds) {
        profileDetails.setText (profile.getProfileDescription ());
        table.setItems (FXCollections.observableArrayList (bonds));
        this.profile = profile;
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
