package ru.shemplo.tbs.gfx.launcher;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener.Change;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
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
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.entity.OperationTypeCategory;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIUtils;
import ru.shemplo.tbs.gfx.component.EnumSelectionSet;
import ru.shemplo.tbs.gfx.component.TileWithHeader;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;

public class TBSStatistics {
    
    private final Stage stage;
    private final Pane root;
    
    public TBSStatistics (Window window) {
        root = new VBox ();
        
        final var scene = new Scene (root);
        
        stage = new Stage ();
        root.getChildren ().add (makeLoadingLayout ());
        
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Launcher | Statistics"));
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
    
    private EnumSelectionSet <OperationTypeCategory> categoriesSelect;
    private TableView <StatisticsData> table;
    private PieChart chart;
    
    private Parent makeStatisticsLayout () {
        final var column = new VBox (8.0);
        VBox.setVgrow (column, Priority.ALWAYS);
        //column.setPadding (new Insets (8.0));
        column.setAlignment (Pos.CENTER);
        
        final var row0 = new HBox (8.0);
        column.getChildren ().add (row0);
        
        chart = new PieChart ();
        chart.setLabelLineLength (10.0);
        chart.setStartAngle (90.0);
        row0.getChildren ().add (chart);
        
        final var columnRight = new VBox (8.0);
        row0.getChildren ().add (columnRight);
        
        final var grThreshold = TBSStyles.<StatisticsData, Number> threshold (0.0, 1e-6);
        
        table = new TableView <StatisticsData> ();
        table.getStylesheets ().setAll (STYLE_TABLES);
        table.setBackground (TBSStyles.BG_TABLE);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        table.setMinWidth (540.0);
        columnRight.getChildren ().add (table);
        
        table.getColumns ().add (TBSUIUtils.<StatisticsData, String> buildTBSTableColumn ()
            .name ("Op. type category").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (125.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.category ().getText ()))
            .converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<StatisticsData, Double> buildTBSTableColumn ()
            .name ("%").tooltip (null)
            .alignment (Pos.BASELINE_RIGHT).minWidth (50.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.percentage () * 100))
            .converter (null).highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<StatisticsData, Integer> buildTBSTableColumn ()
            .name ("Ops").tooltip (null)
            .alignment (Pos.BASELINE_RIGHT).minWidth (50.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.operations ()))
            .tooltip ("Total number of operations of particular type")
            .converter (null).highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<StatisticsData, Number> buildTBSTableColumn ()
            .name ("Max").tooltip (null)
            .alignment (Pos.BASELINE_RIGHT).minWidth (70.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.max ()))
            .converter (null).highlighter (grThreshold)
            .build ());
        table.getColumns ().add (TBSUIUtils.<StatisticsData, Number> buildTBSTableColumn ()
            .name ("Min").tooltip (null)
            .alignment (Pos.BASELINE_RIGHT).minWidth (70.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.min ()))
            .converter (null).highlighter (grThreshold)
            .build ());
        table.getColumns ().add (TBSUIUtils.<StatisticsData, Number> buildTBSTableColumn ()
            .name ("Total value").tooltip (null)
            .alignment (Pos.BASELINE_RIGHT).minWidth (100.0).sortable (false)
            .propertyFetcher (data -> new SimpleObjectProperty <> (data.amount ()))
            .converter (null).highlighter (grThreshold)
            .build ());
        
        categoriesSelect = EnumSelectionSet.allOf (OperationTypeCategory.class);
        categoriesSelect.setOptionsNameConverter (OperationTypeCategory::getText);
        VBox.setMargin (categoriesSelect, new Insets (8.0, 0.0, 8.0, 0.0));
        categoriesSelect.getObservableOptions ().addListener ((Change <?> change) -> {
            TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
                final var data = makeStatisticsData ();
                
                Platform.runLater (() -> {
                    table.getItems ().setAll (data.table ());
                    chart.getData ().setAll (data.chart ());
                });
            });
        });
        
        final var categoriesSelectTitle = new TileWithHeader <> (
            "Considered operation type categories", 
            categoriesSelect
        );
        columnRight.getChildren ().add (categoriesSelectTitle);
        
        return column;
    }
    
    private static OperationTypeCategory fetchOperationTypeCategory (OperationType type) {
        return switch (type) {
            case 
                OPERATION_TYPE_BENEFIT_TAX, 
                OPERATION_TYPE_BENEFIT_TAX_PROGRESSIVE,
                OPERATION_TYPE_BOND_TAX, 
                OPERATION_TYPE_BOND_TAX_PROGRESSIVE,
                OPERATION_TYPE_DIVIDEND_TAX, 
                OPERATION_TYPE_DIVIDEND_TAX_PROGRESSIVE,
                OPERATION_TYPE_TAX, 
                OPERATION_TYPE_TAX_PROGRESSIVE, 
                OPERATION_TYPE_TAX_REPO, 
                OPERATION_TYPE_TAX_REPO_PROGRESSIVE,
                OPERATION_TYPE_TAX_REPO_HOLD, 
                OPERATION_TYPE_TAX_REPO_HOLD_PROGRESSIVE 
            -> OperationTypeCategory.TAX;
            
            case
                OPERATION_TYPE_BROKER_FEE,
                OPERATION_TYPE_MARGIN_FEE,
                OPERATION_TYPE_SERVICE_FEE,
                OPERATION_TYPE_SUCCESS_FEE,
                OPERATION_TYPE_TRACK_MFEE,
                OPERATION_TYPE_TRACK_PFEE
            -> OperationTypeCategory.FEE;
            
            case 
                OPERATION_TYPE_BUY,
                OPERATION_TYPE_BUY_CARD,
                OPERATION_TYPE_BUY_MARGIN
            -> OperationTypeCategory.BOND_BUY;
            
            case 
                OPERATION_TYPE_COUPON
            -> OperationTypeCategory.BOND_COUPON;
            
            case
                OPERATION_TYPE_BOND_REPAYMENT,
                OPERATION_TYPE_BOND_REPAYMENT_FULL
            -> OperationTypeCategory.BOND_REPAYMENT;
            
            case
                OPERATION_TYPE_SELL,
                OPERATION_TYPE_SELL_CARD,
                OPERATION_TYPE_SELL_MARGIN
            -> OperationTypeCategory.BOND_SELL;
            
            default -> null;
        };
    }
    
    private volatile Map <OperationTypeCategory, List <Operation>> type2operations = Map.of ();
    
    private final Comparator <StatisticsData> ENTRIES_COMPARATOR = Comparator
          . <StatisticsData, Double> comparing (StatisticsData::absAmount).reversed ()
          . thenComparing (ent -> ent.category ().getText ());
    
    private StatisticsDataBundle makeStatisticsData () {
        final var selectedCategories = categoriesSelect.getOptions ();
        var statistics = Arrays.stream (OperationTypeCategory.values ())
            . filter (selectedCategories::contains)
            . map (cat -> {
                final var operations = type2operations.getOrDefault (cat, List.of ()).stream ()
                    . filter (op -> op.getCurrency ().equalsIgnoreCase ("rub"))
                    . toList ();
                
                final var max = operations.stream ().mapToDouble (cat::getSumValue).max ().orElse (0.0);
                final var min = operations.stream ().mapToDouble (cat::getSumValue).min ().orElse (0.0);
                final var sum = operations.stream ().mapToDouble (cat::getSumValue).sum ();
                return new StatisticsData (cat, 0.0, operations.size (), sum, max, min);
            }).toList ();
        
        final var total = statistics.stream ().mapToDouble (StatisticsData::absAmount).sum ();
        statistics = statistics.stream ().map (data -> data.normalized (total)).toList ();
        
        final var chartData = statistics.stream ()
            . sorted (ENTRIES_COMPARATOR)
            . map (StatisticsData::toChartData)
            . toList ();
        final var tableData = statistics.stream ()
            . sorted (ENTRIES_COMPARATOR)
            . toList ();
        
        return new StatisticsDataBundle (chartData, tableData);
    }
    
    private record StatisticsData (
        OperationTypeCategory category, double percentage, 
        int operations, double amount, double max, double min
    ) {
        
        public double absAmount () {
            return Math.abs (amount ());
        }
        
        public StatisticsData normalized (double total) {
            final var norma = total != 0.0 ? Math.abs (amount) / total : 0.0;
            return new StatisticsData (category (), norma, operations (), amount (), max (), min ());
        }
        
        public PieChart.Data toChartData () {
            return new Data (
                String.format ("%s (%.1f%%)", category ().getText (), percentage () * 100.0),
                percentage ()
            );
        }
        
    }
    
    private record StatisticsDataBundle (List <Data> chart, List <StatisticsData> table) {}
    
    public void loadStatistics (IProfile profile) {
        TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
            final var logger = new TBSLogWrapper ();
            try {
                Platform.runLater (() -> commentT.setText ("Preparing for loading data from Tinkoff..."));
                
                final var client = TBSClient.getInstance ().getConnection (profile, logger);
                final var accounts = client.getUserService ().getAccountsSync ();
                
                final var time = OffsetTime.now ();
                
                final var to = Instant.from (TBSConstants.FAR_PAST.plusYears (100).atTime (time));
                final var from = Instant.from (TBSConstants.FAR_PAST.atTime (time));
                
                Platform.runLater (() -> commentT.setText ("Loading operations from Tinkoff..."));
                type2operations = client.getOperationsService ()
                    . getExecutedOperationsSync (accounts.get (0).getId (), from, to).stream ()
                    . filter (op -> "bond".equalsIgnoreCase (op.getInstrumentType ()))
                    . filter (op -> fetchOperationTypeCategory (op.getOperationType ()) != null)
                    . collect (Collectors.groupingBy (op -> fetchOperationTypeCategory (op.getOperationType ())));
                Platform.runLater (() -> commentT.setText ("Everything is done"));
                
                final var statisticsLayout = makeStatisticsLayout ();
                final var statistics = makeStatisticsData ();
                
                Platform.runLater (() -> {
                    table.getItems ().setAll (statistics.table ());
                    chart.getData ().setAll (statistics.chart ());
                    
                    root.getChildren ().setAll (statisticsLayout);
                    stage.sizeToScene ();
                });
            } catch (IOException ioe) {
                logger.error ("Failed to load statistics", ioe);
            }
        });
    }
    
}
