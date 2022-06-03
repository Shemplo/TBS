package ru.shemplo.tbs.gfx.launcher;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSClient;
import ru.shemplo.tbs.TBSConstants;
import ru.shemplo.tbs.TBSLogWrapper;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.entity.OperationTypeCategory;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIUtils;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Coupon;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.utils.MapperUtils;

public class TBSPendingCoupons {
    
    private final Stage stage;
    private final Pane root;
    
    public TBSPendingCoupons (Window window) {
        root = new VBox ();
        
        final var scene = new Scene (root);
        
        stage = new Stage ();
        root.getChildren ().add (makeLoadingLayout ());
        
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Launcher | Pending coupons"));
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.initModality (Modality.WINDOW_MODAL);
        stage.setResizable (false);
        stage.initOwner (window);
        stage.setScene (scene);
        stage.setHeight (200);
        stage.setWidth (400);
        stage.show ();
    }
    
    private Text commentT;
    
    private Parent makeLoadingLayout () {
        final var column = new VBox (8.0);
        VBox.setVgrow (column, Priority.ALWAYS);
        column.setAlignment (Pos.CENTER);
        
        final var progressLine = new HBox ();
        progressLine.setAlignment (Pos.CENTER);
        progressLine.setFillHeight (false);
        column.getChildren ().add (progressLine);
        
        final var progressPB = new ProgressBar ();
        progressPB.setMinWidth (200);
        progressLine.getChildren ().add (progressPB);
        
        final var commentLine = new HBox ();
        commentLine.setAlignment (Pos.CENTER);
        commentLine.setFillHeight (false);
        column.getChildren ().add (commentLine);
        
        commentT = new Text ();
        commentLine.getChildren ().add (commentT);
        
        return column;
    }
    
    private TableView <PendingCoupons> table;
    
    private Parent makePendingCouponsLayout () {
        final var column = new VBox (8.0);
        VBox.setVgrow (column, Priority.ALWAYS);
        //column.setPadding (new Insets (8.0));
        column.setAlignment (Pos.CENTER);
        
        table = new TableView <PendingCoupons> ();
        table.getStylesheets ().setAll (STYLE_TABLES);
        TBSUIUtils.enableTableDraggingScroll (table);
        table.setBackground (TBSStyles.BG_TABLE);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        table.setMinHeight (500.0);
        table.setMinWidth (850.0);
        column.getChildren ().add (table);
        
        final var grThreshold = TBSStyles.<PendingCoupons, Number> threshold (0.0, 1e-6);
        
        table.getColumns ().add (TBSUIUtils.<PendingCoupons, String> buildTBSTableColumn ()
            .name ("Name").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (300.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.name ()))
            .converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<PendingCoupons, String> buildTBSTableColumn ()
            .name ("Ticker").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (125.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.ticker ()))
            .converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<PendingCoupons, LocalDate> buildTBSTableColumn ()
            .name ("Coupon date").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (100.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.date ()))
            .highlighter (null).converter ((c, v) -> String.valueOf (v))
            .build ());
        table.getColumns ().add (TBSUIUtils.<PendingCoupons, Number> buildTBSTableColumn ()
            .name ("Qty.").tooltip ("Considered amount of lots by the end of coupon period")
            .alignment (Pos.BASELINE_LEFT).minWidth (50.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.quantity ()))
            .converter (null).highlighter (grThreshold)
            .build ());
        table.getColumns ().add (TBSUIUtils.<PendingCoupons, Number> buildTBSTableColumn ()
            .name ("Amount").tooltip ("Excected income according to published data and number of lots")
            .alignment (Pos.BASELINE_LEFT).minWidth (100.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.amount ()))
            .converter (null).highlighter (grThreshold)
            .build ());
        table.getColumns ().add (TBSUIUtils.<PendingCoupons, Number> buildTBSTableColumn ()
            .name ("Payment").tooltip ("Income that was payed by emmitter in fact")
            .alignment (Pos.BASELINE_LEFT).minWidth (100.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.payment ()))
            .converter (null).highlighter (grThreshold)
            .build ());
        
        return column;
    }
    
    private record PendingCoupons (String ticker, String name, LocalDate date, long quantity, double amount, double payment) {}
    
    public void loadPendingCoupons (IProfile profile) {
        TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
            final var logger = new TBSLogWrapper ();
            try {
                Platform.runLater (() -> commentT.setText ("Preparing for loading data from Tinkoff..."));
                
                final var client = TBSClient.getInstance ().getConnection (profile, logger);
                final var accounts = client.getUserService ().getAccountsSync ();
                final var accountId = accounts.get (0).getId ();
                
                //final var nowTimestamp = Instant.now ().getEpochSecond ();
                final var time = OffsetTime.now ();
                
                final var from = Instant.from (TBSConstants.NOW.minusMonths (1L).atTime (time));
                final var to = Instant.from (TBSConstants.NOW.plusMonths (1L).atTime (time));
                
                Platform.runLater (() -> commentT.setText ("Loading portfolio bonds from Tinkoff..."));
                final var bonds = client.getOperationsService ().getPortfolioSync (accountId).getPositions ().stream ()
                    . filter (pos -> "bond".equalsIgnoreCase (pos.getInstrumentType ()))
                    . toList ();
                final var figi2bond = bonds.stream ()
                    . map (bond -> client.getInstrumentsService ().getBondByFigiSync (bond.getFigi ()))
                    . collect (Collectors.toMap (Bond::getFigi, Function.identity ()));
                Platform.runLater (() -> commentT.setText ("Loading bonds coupons from Tinkoff..."));
                final var bond2coupons = bonds.stream ()
                    . flatMap (bond -> client.getInstrumentsService ().getBondCouponsSync (bond.getFigi (), from, to).stream ())
                    //. filter (coupon -> coupon.getFixDate ().getSeconds () <= nowTimestamp)
                    . sorted (Comparator.comparing (coupon -> coupon.getCouponDate ().getSeconds ()))
                    . collect (Collectors.groupingBy (Coupon::getFigi));
                
                final var pendingCoupons = new ArrayList <PendingCoupons> ();
                
                Platform.runLater (() -> commentT.setText ("Processing loaded data..."));
                bond2coupons.forEach ((bondFigi, coupons) -> {
                    if (coupons.isEmpty ()) { return; }
                    
                    final var bond = figi2bond.get (bondFigi);
                    
                    final var couponFrom = Instant.from (TBSConstants.FAR_PAST.atTime (time));
                    final var couponTo = Instant.now ();
                    
                    final var operations = client.getOperationsService ().getExecutedOperationsSync (
                        accountId, couponFrom, couponTo, bondFigi
                    ).stream ()
                    . sorted (Comparator.comparing (op -> op.getDate ().getSeconds ()))
                    . toList ();
                    
                    final var currentValue = new AtomicLong (0);
                    final var time2amount = operations.stream ()
                        . filter (op -> op.getOperationType () == OperationType.OPERATION_TYPE_BUY 
                                     || op.getOperationType () == OperationType.OPERATION_TYPE_SELL
                        )
                        . collect (Collectors.toMap (
                            op -> op.getDate ().getSeconds (),
                            op -> {
                                long delta = op.getQuantity () - op.getQuantityRest ();
                                if (op.getOperationType () == OperationType.OPERATION_TYPE_SELL) {
                                    delta *= -1L;
                                }
                                
                                return currentValue.addAndGet (delta);
                            }, 
                            (a, b) -> a, TreeMap::new
                        ));
                    final var time2operation = operations.stream ()
                        . filter (op -> op.getOperationType () == OperationType.OPERATION_TYPE_COUPON)
                        . collect (Collectors.toMap (
                            op -> op.getDate ().getSeconds (), 
                            Function.identity (), 
                            (a, b) -> a, TreeMap::new
                        ));
                    
                    //System.out.println ("Bond: " + bond.getName () + " / " + bond.getTicker ()); // SYSOUT
                    //System.out.println ("Time -> amount: " + time2amount); // SYSOUT
                    
                    for (final var coupon : coupons) {
                        final var seconds = coupon.getFixDate ().getSeconds ();
                        //System.out.println ("  Coupon: " + seconds); // SYSOUT
                        
                        final var operation = TBSUtils.mapIfNN (time2operation.ceilingEntry (seconds), Entry::getValue, null);
                        final var quantity = TBSUtils.mapIfNN (time2amount.floorEntry (seconds), Entry::getValue, 0L);
                        final var price = MapperUtils.moneyValueToBigDecimal (coupon.getPayOneBond ());
                        
                        /*
                        System.out.println ("    Operation: " + TBSUtils.mapIfNN (operation, op -> String.format (
                            "(seconds: %d, payment: %s)", op.getDate ().getSeconds (), MapperUtils.moneyValueToBigDecimal (op.getPayment ())
                        ), "(none)")); // SYSOUT
                        System.out.println ("    Quantity: " + quantity); // SYSOUT
                        System.out.println ("    Amount: " + price); // SYSOUT
                        */
                        
                        // Do not consider this operation any more
                        TBSUtils.doIfNN (operation, op -> time2operation.remove (op.getDate ().getSeconds ()));
                        
                        final var amount = price.multiply (BigDecimal.valueOf (quantity)).doubleValue ();
                        
                        if (quantity > 0) {
                            final var date = LocalDate.ofInstant (new Date (seconds * 1000).toInstant (), ZoneId.systemDefault ());                            
                            final var payment = TBSUtils.mapIfNN (operation, OperationTypeCategory.BOND_COUPON::getSumValue, 0.0);
                            
                            pendingCoupons.add (new PendingCoupons (bond.getTicker (), bond.getName (), date, quantity, amount, payment));
                        }
                    }
                    
                    //System.out.println (); // SYSOUT
                });
                
                pendingCoupons.sort (Comparator.comparing (PendingCoupons::date));
                
                Platform.runLater (() -> commentT.setText ("Everything is done"));
                
                final var pendingLayout = makePendingCouponsLayout ();
                
                Platform.runLater (() -> {                    
                    root.getChildren ().setAll (pendingLayout);
                    table.getItems ().setAll (pendingCoupons);
                    stage.sizeToScene ();
                });
            } catch (IOException ioe) {
                logger.error ("Failed to load statistics", ioe);
            }
        });
    }
    
}
