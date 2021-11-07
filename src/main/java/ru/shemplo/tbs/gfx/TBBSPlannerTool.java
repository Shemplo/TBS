package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.TBSConstants.*;
import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.time.LocalDate;

import com.panemu.tiwulfx.control.NumberField;

import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ru.shemplo.tbs.MappingROProperty;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSPlanner;
import ru.shemplo.tbs.TBSPlanner.DistributionCategory;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.IPlanningBond;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.shemplo.tbs.gfx.table.TBSEditTableCell;

public class TBBSPlannerTool extends HBox {
    
    private AreaChart <Number, Number> distributionChart;
    private ChoiceBox <DistributionCategory> typeSelect;
    private DoubleProperty diversificationProperty;
    private NumberField <Double> amountField;
    private TableView <IPlanningBond> table;
    
    public TBBSPlannerTool () {
        setPadding (new Insets (2, 0, 0, 0));
        setFillHeight (true);
        
        getChildren ().add (makeLeftPannel ());
        
        getChildren ().add (table = makeTable ());
        HBox.setHgrow (table, Priority.ALWAYS);
        
        TBSPlanner.getInstance ().getBonds ().addListener ((ListChangeListener <IPlanningBond>) (change -> {
            TBSPlanner.getInstance ().updateDistribution ();
            updateChart ();
        }));
        
        updateChart ();
    }
    
    private Parent makeLeftPannel () {
        final var scroll = new ScrollPane ();
        scroll.setPadding (new Insets (10, 16, 12, 16));
        scroll.setBackground (null);
        scroll.setMinWidth (352);
        scroll.setBorder (null);
        
        final var column = new VBox (4);
        column.setFillWidth (false);
        scroll.setContent (column);
                
        final var line1 = new HBox (4);
        column.getChildren ().add (line1);
        
        final var typeHeader = new Text ("To distribute");
        typeHeader.setWrappingWidth (100.0);
        line1.getChildren ().add (typeHeader);
        
        final var amounHeader = new Text ("Amount");
        amounHeader.setWrappingWidth (200.0);
        line1.getChildren ().add (amounHeader);
        
        final var line2 = new HBox (4);
        VBox.setMargin (line2, new Insets (0, 0, 12, 0));
        column.getChildren ().add (line2);
        
        typeSelect = new ChoiceBox <DistributionCategory> ();
        typeSelect.getItems ().setAll (DistributionCategory.values ());
        typeSelect.setMinWidth (typeHeader.getWrappingWidth ());  
        typeSelect.setValue (DistributionCategory.SUM);
        typeSelect.setValue (TBSPlanner.getInstance ().getCategory ());
        line2.getChildren ().add (typeSelect);
        
        amountField = new NumberField <> (Double.class);
        amountField.setMinWidth (amounHeader.getWrappingWidth ());
        amountField.setValue (TBSPlanner.getInstance ().getAmount ());
        line2.getChildren ().add (amountField);
        
        final var line3 = new HBox (16);
        line3.setAlignment (Pos.BOTTOM_LEFT);
        column.getChildren ().add (line3);
        
        line3.getChildren ().add (new Text ("Diversification, %"));
        
        final var customDiversificationIcon = new ImageView (TBSApplicationIcons.warning);
        customDiversificationIcon.setVisible (false);
        customDiversificationIcon.setFitHeight (12);
        customDiversificationIcon.setFitWidth (12);
        line3.getChildren ().add (customDiversificationIcon);
        
        final var line4 = new HBox (16);
        VBox.setMargin (line4, new Insets (0, 0, 12, 0));
        column.getChildren ().add (line4);
        
        final var diversificationSlider = new Slider (0.0, 100.0, 100.0);
        diversificationSlider.setShowTickLabels (true);
        diversificationSlider.setShowTickMarks (true);
        diversificationSlider.setMinWidth (235.0);
        line4.getChildren ().add (diversificationSlider);
        
        final var diversificationField = new NumberField <> ();
        diversificationField.setMaxWidth (
            typeSelect.getMinWidth () + amountField.getMinWidth () + line2.getSpacing () 
            - diversificationSlider.getMinWidth () - line4.getSpacing ()
        );
        line4.getChildren ().add (diversificationField);
        
        final var xAxis = new NumberAxis ();
        //xAxis.setMinorTickVisible (true);
        
        distributionChart = new AreaChart <> (xAxis, new NumberAxis ());
        distributionChart.setMinWidth (typeSelect.getMinWidth () + amountField.getMinWidth () + line2.getSpacing ());
        distributionChart.setMaxWidth  (distributionChart.getMinWidth ());
        distributionChart.setMaxHeight (distributionChart.getMaxWidth () * 1.5);
        VBox.setMargin (distributionChart, new Insets (0, 0, 24, 0));
        distributionChart.setCreateSymbols (false);
        distributionChart.setAnimated (false);
        column.getChildren ().add (distributionChart);
        
        final var line5 = new HBox (4);
        column.getChildren ().add (line5);
        
        final var priceHeader = new Text ("Total price");
        priceHeader.setWrappingWidth (200.0);
        line5.getChildren ().add (priceHeader);
        
        final var lotsHeader = new Text ("Total lots");
        amounHeader.setWrappingWidth (100.0);
        line5.getChildren ().add (lotsHeader);
        
        final var line6 = new HBox (4);
        column.getChildren ().add (line6);
        
        final var priceField = new NumberField <> ();
        priceField.valueProperty ().bindBidirectional (TBSPlanner.getInstance ().getSummaryPrice ());
        priceField.setMinWidth (200.0); priceField.setMaxWidth (priceField.getMinWidth ());
        priceField.setEditable (false);
        //priceField.setDisable (true);
        line6.getChildren ().add (priceField);
        
        final var lotsField = new NumberField <> ();
        lotsField.valueProperty ().bindBidirectional (TBSPlanner.getInstance ().getSummaryLots ());
        lotsField.setMinWidth (100.0); lotsField.setMaxWidth (lotsField.getMinWidth ());
        lotsField.setEditable (false);
        //priceField.setDisable (true);
        line6.getChildren ().add (lotsField);
        
        final var realSeries = new Series <Number, Number> ();
        realSeries.setName ("Real distr.");
        distributionChart.getData ().add (realSeries);
        
        final var calculatedSeries = new Series <Number, Number> ();
        calculatedSeries.setName ("Calculated distr.");
        distributionChart.getData ().add (calculatedSeries);
        
        typeSelect.valueProperty ().addListener ((__, ___, value) -> {
            TBSPlanner.getInstance ().updateParameters (
                value, TBSUtils.aOrB (amountField.getValue (), 0.0), 
                diversificationProperty.get ()
            );
            updateChart ();
        });
        
        amountField.valueProperty ().addListener ((__, ___, value) -> {
            TBSPlanner.getInstance ().updateParameters (
                typeSelect.getValue (), TBSUtils.aOrB (value, 0.0), 
                diversificationProperty.get ()
            );
            updateChart ();
        });
        
        diversificationProperty = diversificationSlider.valueProperty ();
        diversificationProperty.bindBidirectional (diversificationField.valueProperty ());
        diversificationSlider.setValue (TBSPlanner.getInstance ().getDiversification ());
        diversificationProperty.addListener ((__, ___, div) -> {
            TBSPlanner.getInstance ().updateParameters (
                typeSelect.getValue (), TBSUtils.aOrB (amountField.getValue (), 0.0), 
                diversificationProperty.get ()
            );
            updateChart ();
        });
        
        return scroll;
    }
    
    private TableView <IPlanningBond> makeTable () {
        final var table = new TableView <IPlanningBond> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        //HBox.setMargin (table, new Insets (0, 2, 2, 0));
        table.getStylesheets ().setAll (STYLE_TABLES);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var grThreshold = TBSStyles.<IPlanningBond, Number> threshold (0.0, 1e-6);
        final var sameMonth = TBSStyles.<IPlanningBond> sameMonth (NOW);
        
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, Integer> buildTBSTableColumn ()
            .name ("#").tooltip (null)
            .alignment (Pos.BASELINE_CENTER).minWidth (30.0).sortable (false)
            .propertyFetcher (bond -> bond.getProperty (IPlanningBond.INDEX_PROPERTY, () -> 0, false))
            .converter (null).highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, String> buildTBSTableColumn ()
            .name ("Name").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (250.0).sortable (false)
            .propertyFetcher (bond -> new MappingROProperty <> (
                bond.getRWProperty ("code", () -> ""), 
                TBSBondManager::getBondName
            )).converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, String> buildTBSTableColumn ()
            .name ("Ticker").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (125.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("code", () -> "")).converter ((r, v) -> v)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, Number> buildTBSTableColumn ()
            .name ("Score").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
            .propertyFetcher (bond -> new MappingROProperty <> (
                bond.getRWProperty ("code", () -> ""), 
                TBSBondManager::getBondScore
            ))
            .highlighter (grThreshold).converter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, Number> buildTBSTableColumn ()
            .name ("Price").tooltip ("Last committed price in MOEX")
            .alignment (Pos.BASELINE_LEFT).minWidth (80.0).sortable (false)
            .propertyFetcher (bond -> new MappingROProperty <> (
                bond.getRWProperty ("code", () -> ""), 
                TBSBondManager::getBondPrice
            ))
            .highlighter (grThreshold).converter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, LocalDate> buildTBSTableColumn ()
            .name ("Next C").tooltip ("Closest date of the next coupon")
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (bond -> new MappingROProperty <> (
                bond.getRWProperty ("code", () -> ""), 
                TBSBondManager::getBondNextCoupon
            ))
            .highlighter (sameMonth).converter ((c, v) -> String.valueOf (v))
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, Integer> buildTBSTableColumn ()
            .name ("📊").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (50.0).sortable (false)
            .propertyFetcher (bond -> bond.getRWProperty ("amount", () -> 0))
            .converter (null).highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IPlanningBond, Integer, NumberField <Integer>> buildTBSEditTableColumn ()
            .name ("").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (100.0).sortable (false)
            .propertyFetcher (bond -> new MappingROProperty <> (
                bond.<Integer> getRWProperty ("customValue", () -> null), 
                v -> new LinkedObject <Integer> (bond.getCode (), v)
            ))
            .fieldSupplier (this::makeCustomLotsValueField)
            .converter (null).highlighter (null)
            .build ());
        
        return table;
    }
    
    private NumberField <Integer> makeCustomLotsValueField (TBSEditTableCell <IPlanningBond, Integer, NumberField <Integer>> cell) {
        final var field = new NumberField <> (Integer.class);
        field.setPadding (new Insets (1, 4, 1, 4));
        field.valueProperty ().addListener ((__, ___, value) -> {
            TBSUtils.doIfNN (cell.getItem (), item -> {
                final var planner = TBSPlanner.getInstance ();
                final var bond = planner.getBondByTicker (item.getLink ());
                
                TBSUtils.doIfNN (bond, b -> {
                    b.getRWProperty ("customValue", () -> 0).set (value);
                    planner.updateDistribution ();
                    planner.dump ();
                    updateChart ();
                });
            });
        });
        
        return field;
    }
    
    public void applyData (ITBSProfile profile) {
        final var bonds = TBSPlanner.getInstance ().getBonds ();
        if (table.getItems () != bonds) {            
            table.setItems (bonds);
        }
    }
    
    private void updateChart () {
        final var bonds = TBSPlanner.getInstance ().getBonds ();
        if (bonds.isEmpty ()) { return; }
        
        final var calculatedSeries = distributionChart.getData ().get (1);
        calculatedSeries.getData ().clear ();
        
        final var realSeries = distributionChart.getData ().get (0);
        realSeries.getData ().clear ();
        
        for (int i = 0; i < bonds.size (); i++) {
            final var bond = bonds.get (i);
            final var calculated = bond.getCalculatedAmount ();
            calculatedSeries.getData ().add (new Data <> (i, calculated));
            calculatedSeries.getData ().add (new Data <> (i + 1, calculated));
            realSeries.getData ().add (new Data <> (i, bond.getCurrentValue ()));
            realSeries.getData ().add (new Data <> (i + 1, bond.getCurrentValue ()));
        }
        calculatedSeries.getData ().add (new Data <> (bonds.size (), 0.0));
        realSeries.getData ().add (new Data <> (bonds.size (), 0.0));
    }
    
}
