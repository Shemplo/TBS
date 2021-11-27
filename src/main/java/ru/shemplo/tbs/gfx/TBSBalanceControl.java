package ru.shemplo.tbs.gfx;

import java.time.LocalDate;
import java.util.HashMap;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSConstants;
import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSPlanner;

public class TBSBalanceControl extends ScrollPane {
    
    private XYChart <String, Number> balanceChart, creditsChart;
    
    public TBSBalanceControl () {
        setPadding (new Insets (2, 0, 0, 0));
        setFitToHeight (true);
        setFitToWidth (true);
        setMinHeight (500);
        
        final var content = new VBox ();
        content.setPadding (new Insets (10, 16, 12, 16));
        //content.setBackground (TBSStyles.BG_SO_CLOSE);
        content.setFillWidth (true);
        setContent (content);
        
        content.getChildren ().add (makeTopChart ());
        
        updateChart ();
    }
    
    private Parent makeTopChart () {
        final var chart = new StackPane ();
        
        final var datesAxis = new CategoryAxis ();
        final var valuesAxis = new NumberAxis ();
        valuesAxis.setAutoRanging (false);
        
        final var pBalanceSeries = new Series <String, Number> (); // 1
        final var pCreditsSeries = new Series <String, Number> (); // 1
        final var balanceSeries = new Series <String, Number> ();  // 0
        final var creditsSeries = new Series <String, Number> ();  // 0
        
        balanceChart = new StackedAreaChart <> (datesAxis, valuesAxis);
        balanceChart.setLegendVisible (false);
        balanceChart.maxHeight (100.0);
        chart.getChildren ().add (balanceChart);
        
        balanceChart.getData ().add (balanceSeries);
        balanceChart.getData ().add (pBalanceSeries);
        
        creditsChart = new StackedBarChart <> (datesAxis, valuesAxis);
        creditsChart.getStylesheets ().add (TBSStyles.STYLE_MULTI_CHART);
        creditsChart.maxHeight (balanceChart.getMaxHeight ());
        creditsChart.setLegendVisible (false);
        chart.getChildren ().add (creditsChart);
        
        creditsChart.getData ().add (creditsSeries);
        creditsChart.getData ().add (pCreditsSeries);
        
        return chart;
    }
    
    public void updateChart () {
        final var balanceSeries = balanceChart.getData ().get (0);
        balanceSeries.getData ().clear ();
        
        final var pBalanceSeries = balanceChart.getData ().get (1);
        pBalanceSeries.getData ().clear ();
        
        final var creditsSeries = creditsChart.getData ().get (0);
        creditsSeries.getData ().clear ();
        
        final var pCreditsSeries = creditsChart.getData ().get (1);
        pCreditsSeries.getData ().clear ();
        
        final var date2credits = new HashMap <LocalDate, Double> ();
        for (final var bond : TBSBondManager.getInstance ().getPortfolio ()) {
            final var toRUBCoeff = TBSCurrencyManager.getInstance ().getToRubCoefficient (bond.getCurrency ());
            for (final var coupon : bond.getCoupons ()) {
                date2credits.compute (coupon.getDate (), (k, v) -> (v == null ? 0 : v) + coupon.getAmount () * toRUBCoeff * bond.getLots ());
            }
        }
        final var date2pCredits = new HashMap <LocalDate, Double> (); // planned credits
        for (final var pbond : TBSPlanner.getInstance ().getBonds ()) { 
            final var bond = TBSBondManager.getBondByTicker (pbond.getCode (), false);
            
            final var toRUBCoeff = TBSCurrencyManager.getInstance ().getToRubCoefficient (bond.getCurrency ());
            final var lots = pbond.getCurrentValue ();
            for (final var coupon : bond.getCoupons ()) {
                if (coupon.isNextCoupon ()) { // do not consider next coupon
                    continue;
                }
                
                date2pCredits.compute (coupon.getDate (), (k, v) -> (v == null ? 0 : v) + coupon.getAmount () * toRUBCoeff * lots);
            }
        }
        
        double maxY = 0.0, sumCredits = 0.0, sumPCredits = 0.0;
        for (int i = 0; i < 120; i++) {
            final var date = TBSConstants.NOW.plusDays (i);
            final var value = date2credits.getOrDefault (date, 0.0);
            sumCredits += value;
            creditsSeries.getData ().add (new Data <> (date.toString (), value));
            balanceSeries.getData ().add (new Data <> (date.toString (), sumCredits));
            
            final var pValue = date2pCredits.getOrDefault (date, 0.0);
            sumPCredits += pValue;
            pCreditsSeries.getData ().add (new Data <> (date.toString (), pValue));
            pBalanceSeries.getData ().add (new Data <> (date.toString (), sumPCredits));
            
            maxY = Math.max (value + pValue, sumCredits + sumPCredits);
        }
        
        final var axisY = (NumberAxis) balanceChart.getYAxis ();
        axisY.setUpperBound (maxY * 1.05);
        axisY.setTickUnit (axisY.getUpperBound () / 10.0);
    }
    
}
