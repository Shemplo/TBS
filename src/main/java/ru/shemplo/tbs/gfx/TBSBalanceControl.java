package ru.shemplo.tbs.gfx;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TBSBalanceControl extends VBox {
    
    public TBSBalanceControl () {
        setPadding (new Insets (2, 0, 0, 0));
        setFillWidth (true);
        
        getChildren ().add (makeTopChart ());
    }
    
    private Parent makeTopChart () {
        final var content = new VBox ();
        content.setBackground (TBSStyles.BG_SO_CLOSE);
        content.setFillWidth (true);
        
        final var chartBox = new StackPane ();
        
        final var scroll = new ScrollPane (chartBox);
        scroll.setPadding (new Insets (10, 16, 12, 16));
        scroll.setBackground (Background.EMPTY);
        scroll.setFitToWidth (true);
        scroll.setBorder (null);
        
        final var datesAxis = new CategoryAxis ();
        final var valueAxis = new NumberAxis (0.0, 150.0, 50.0);
        //datesAxis.getCategories ().add (LocalDate.now ().plusDays (0).toString ());
        //datesAxis.getCategories ().add (LocalDate.now ().plusDays (1).toString ());
        
        final var series = new Series <String, Number> ();
        final var series2 = new Series <String, Number> ();
        double sum = 0.0;
        for (int i = 0; i < 30; i++) {
            double value = Math.random () * 10;
            sum += value;
            series.getData ().add (new Data <> (LocalDate.now ().plusDays (i).toString (), value));
            series2.getData ().add (new Data <> (LocalDate.now ().plusDays (i).toString (), sum));
        }
        
        final var balanceChart = new AreaChart <> (datesAxis, valueAxis);
        balanceChart.setLegendVisible (false);
        balanceChart.maxHeight (100.0);
        chartBox.getChildren ().add (balanceChart);
        
        final var creditChart = new StackedBarChart <> (datesAxis, valueAxis);
        creditChart.getStylesheets ().add (TBSStyles.STYLE_MULTI_CHART);
        creditChart.maxHeight (balanceChart.getMaxHeight ());
        creditChart.setLegendVisible (false);
        chartBox.getChildren ().add (creditChart);
        
        balanceChart.getData ().add (series2);
        creditChart.getData ().add (series);
        
        return scroll;
    }
    
}
