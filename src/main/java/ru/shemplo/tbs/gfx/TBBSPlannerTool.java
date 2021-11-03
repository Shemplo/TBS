package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import com.panemu.tiwulfx.control.NumberField;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
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
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSPlanner;
import ru.shemplo.tbs.TBSPlanner.DistributionCategory;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.shemplo.tbs.entity.PlanningBond;

public class TBBSPlannerTool extends HBox {
    
    private LineChart <Number, Number> distributionChart;
    private ChoiceBox <DistributionCategory> typeSelect;
    private DoubleProperty diversificationProperty;
    private NumberField <Double> amountField;
    private TableView <PlanningBond> table;
    
    public TBBSPlannerTool () {
        setPadding (new Insets (2, 0, 0, 0));
        setFillHeight (true);
        
        getChildren ().add (makeLeftPannel ());
        
        getChildren ().add (table = makeTable ());
        HBox.setHgrow (table, Priority.ALWAYS);
        
        TBSPlanner.getInstance ().getBonds ().addListener ((ListChangeListener <PlanningBond>) (change -> {
            TBSPlanner.getInstance ().updateDistribution ();
            updateChart ();
        }));
        
        updateChart ();
    }
    
    private Parent makeLeftPannel () {
        final var column = new VBox (4);
        column.setPadding (new Insets (10, 16, 0, 16));
        column.setFillWidth (false);
                
        final var line1 = new HBox (4);
        column.getChildren ().add (line1);
        
        final var sumHeader = new Text ("To distribute");
        sumHeader.setWrappingWidth (100.0);
        line1.getChildren ().add (sumHeader);
        
        final var lotsHeader = new Text ("Amount");
        lotsHeader.setWrappingWidth (200.0);
        line1.getChildren ().add (lotsHeader);
        
        final var line2 = new HBox (4);
        VBox.setMargin (line2, new Insets (0, 0, 12, 0));
        column.getChildren ().add (line2);
        
        typeSelect = new ChoiceBox <DistributionCategory> ();
        typeSelect.getItems ().setAll (DistributionCategory.values ());
        typeSelect.setMinWidth (sumHeader.getWrappingWidth ());  
        typeSelect.setValue (DistributionCategory.SUM);
        typeSelect.setValue (TBSPlanner.getInstance ().getCategory ());
        line2.getChildren ().add (typeSelect);
        
        amountField = new NumberField <> (Double.class);
        amountField.setMinWidth (lotsHeader.getWrappingWidth ());
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
        
        distributionChart = new LineChart <> (new NumberAxis (), new NumberAxis ());
        distributionChart.setMinWidth (typeSelect.getMinWidth () + amountField.getMinWidth () + line2.getSpacing ());
        distributionChart.setMaxWidth (distributionChart.getMinWidth ());
        distributionChart.setMaxHeight (distributionChart.getMaxWidth ());
        distributionChart.setAnimated (false);
        column.getChildren ().add (distributionChart);
        
        final var realSeries = new Series <Number, Number> ();
        realSeries.setName ("Real distrubution");
        distributionChart.getData ().add (realSeries);
        
        final var idealSeries = new Series <Number, Number> ();
        idealSeries.setName ("Ideal distrubution");
        distributionChart.getData ().add (idealSeries);
        
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
        
        return column;
    }
    
    private TableView <PlanningBond> makeTable () {
        final var table = new TableView <PlanningBond> ();
        table.setBackground (new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        //HBox.setMargin (table, new Insets (0, 2, 2, 0));
        table.getStylesheets ().setAll (STYLE_TABLES);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var grThreshold = TBSStyles.<PlanningBond, Number> threshold (0.0, 1e-6);
        
        table.getColumns ().add (TBSUIUtils.makeTBSTableColumn (
            "#", null, (r, __) -> r.getIndex (), false, 30, Pos.BASELINE_CENTER, null
        ));
        table.getColumns ().add (TBSUIUtils.makeTBSTableColumn (
            "Bond name", null, pb -> TBSBondManager.getBondName (pb.getCode ()), false, 250.0, Pos.BASELINE_LEFT, null
        ));
        table.getColumns ().add (TBSUIUtils.makeTBSTableColumn (
            "Code", "Bond ticker", PlanningBond::getCode, false, 125, Pos.BASELINE_LEFT, null
        ));
        table.getColumns ().add (TBSUIUtils.makeTBSTableColumn (
            "Score", null, PlanningBond::getScore, false, 80, Pos.BASELINE_LEFT, grThreshold
        ));
        table.getColumns ().add (TBSUIUtils.makeTBSTableColumn (
            "Price", null, PlanningBond::getPrice, false, 80, Pos.BASELINE_LEFT, grThreshold
        ));
        table.getColumns ().add (TBSUIUtils.makeTBSTableColumn (
            "📊", null, PlanningBond::getAmount, false, 50, Pos.BASELINE_LEFT, null
        ));
        
        final var customSuggectionColumn = new TableColumn <PlanningBond, PlanningBond> ("");
        customSuggectionColumn.setCellValueFactory (cell -> new SimpleObjectProperty <> (cell.getValue ()));
        customSuggectionColumn.setCellFactory (__ -> new TBSEditableCell <> ((value, bond) -> {
            updateChart (); TBSPlanner.getInstance ().dump ();
        }, 1.0));
        customSuggectionColumn.setSortable (false);
        customSuggectionColumn.setMinWidth (100);
        table.getColumns ().add (customSuggectionColumn);
        
        return table;
    }
    
    public void refreshData (ITBSProfile profile) {
        final var bonds = TBSPlanner.getInstance ().getBonds ();
        if (table.getItems () != bonds) {            
            table.setItems (bonds);
        }
    }
    
    private void updateChart () {
        final var bonds = TBSPlanner.getInstance ().getBonds ();
        if (bonds.isEmpty ()) { return; }
        
        final var idealSeries = distributionChart.getData ().get (1);
        idealSeries.getData ().clear ();
        
        final var realSeries = distributionChart.getData ().get (0);
        realSeries.getData ().clear ();
        
        for (int i = 0; i < bonds.size (); i++) {
            final var bond = bonds.get (i);
            idealSeries.getData ().add (new Data <> (i, bond.getIdealAmount ()));
            idealSeries.getData ().add (new Data <> (i + 1, bond.getIdealAmount ()));
            realSeries.getData ().add (new Data <> (i, bond.getCurrentValue ()));
            realSeries.getData ().add (new Data <> (i + 1, bond.getCurrentValue ()));
        }
        idealSeries.getData ().add (new Data <> (bonds.size (), 0.0));
        realSeries.getData ().add (new Data <> (bonds.size (), 0.0));
    }
    
}
