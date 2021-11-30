package ru.shemplo.tbs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.shemplo.tbs.entity.BalanceScale;
import ru.shemplo.tbs.entity.Credit;
import ru.shemplo.tbs.entity.DateCredit;
import ru.shemplo.tbs.entity.ICredit;

//@Slf4j
@Getter
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSBalanceController {
    
    private static volatile TBSBalanceController instance;
    
    public static TBSBalanceController getInstance () {
        if (instance == null) {
            synchronized (TBSBalanceController.class) {
                if (instance == null) {
                    instance = new TBSBalanceController ();
                }
            }
        }
        
        return instance;
    }
    
    private BalanceScale scale = BalanceScale.DAY;
    private int offset = scale.getDefaultOffset ();
    private int amount = scale.getDefaultAmount ();
    
    private final SimpleDoubleProperty chartUpperBound = new SimpleDoubleProperty (100.0);
    private final ObservableList <DateCredit> credits = FXCollections.observableArrayList ();
    
    public void updateParameters (BalanceScale scale) {
        updateParameters (scale, scale.getDefaultOffset (), scale.getDefaultAmount ());
    }
    
    public void updateParameters (BalanceScale scale, int offset, int amount) {
        if (this.scale != scale || this.offset != offset || this.amount != amount) {            
            this.scale = scale; this.offset = offset; this.amount = amount;
            updateBalance ();
        }
    }
    
    public void updateBalance () {
        final var date2credits = new HashMap <LocalDate, DateCredit> ();
        
        final var from = getFromDate ();
        final var to = getToDate (from);
        
        double totalSum = fillMapWithPortfolio (date2credits, from, to);
        totalSum += fillMapWithPlanned (date2credits, from, to);
        
        final var credits = new ArrayList <DateCredit> ();
        LocalDate current = from;
        
        while (!current.isAfter (to)) {
            final var stub = new DateCredit (current);
            credits.add (date2credits.getOrDefault (current, stub));
            current = getNextPeriodDate (current);
        }
        
        chartUpperBound.set (totalSum * 1.05);
        this.credits.setAll (credits);
    }
    
    private double fillMapWithPortfolio (Map <LocalDate, DateCredit> date2credits, LocalDate from, LocalDate to) {
        double totalSum = 0.0;
        for (final var bond : TBSBondManager.getInstance ().getPortfolio ()) {
            for (final var coupon : bond.getCoupons ()) {
                final var date = getScaledDate (coupon.getDate ());
                if (!date.isBefore (from) && !date.isAfter (to)) {
                    final var credit = Credit.portfolioCoupon (bond, coupon);
                    
                    date2credits.computeIfAbsent (date, __ -> new DateCredit (date)).add (credit);
                    totalSum += credit.getCreditValue ();
                }
            }
            
            final var date = getScaledDate (bond.getEnd ());
            if (!date.isBefore (from) && !date.isAfter (to)) {
                final var credit = Credit.portfolioBondEnd (bond);
                
                date2credits.computeIfAbsent (date, __ -> new DateCredit (date)).add (credit);
                totalSum += credit.getCreditValue ();
            }
        }
        
        return totalSum;
    }
    
    private double fillMapWithPlanned (Map <LocalDate, DateCredit> date2credits, LocalDate from, LocalDate to) {
        double totalSum = 0.0;
        for (final var planningBond : TBSPlanner.getInstance ().getBonds ()) {
            final var bond = TBSBondManager.getBondByTicker (planningBond.getCode (), false);
            final var lots = planningBond.getCurrentValue ();
            
            for (final var coupon : bond.getCoupons ()) {
                if (coupon.isNextCoupon ()) {
                    continue; // do not consider next coupon because it's not in portfolio
                }
                
                final var date = getScaledDate (coupon.getDate ());
                if (!date.isBefore (from) && !date.isAfter (to)) {
                    final var credit = Credit.coupon (bond, lots, coupon);
                    
                    date2credits.computeIfAbsent (date, __ -> new DateCredit (date)).add (credit);
                    totalSum += credit.getCreditValue ();
                }
            }
            
            final var date = getScaledDate (bond.getEnd ());
            if (!date.isBefore (from) && !date.isAfter (to)) {
                final var credit = Credit.bondEnd (bond, lots);
                
                date2credits.computeIfAbsent (date, __ -> new DateCredit (date)).add (credit);
                totalSum += credit.getCreditValue ();
            }
        }
        
        return totalSum;
    }
    
    public LocalDate getScaledDate (LocalDate date) {
        return switch (scale) {
            case DAY -> date;
            case MONTH -> date.withDayOfMonth (1);
            case YEAR -> date.withDayOfYear (1);
            default -> date;
        };
    }
    
    private LocalDate getNextPeriodDate (LocalDate date) {
        return switch (scale) {
            case DAY -> date.plusDays (1);
            case MONTH -> date.plusMonths (1);
            case YEAR -> date.plusYears (1);
            default -> date;
        };
    }
    
    private LocalDate getFromDate () {
        final var date = getScaledDate (TBSConstants.NOW);
        /*
        return switch (scale) {
            case DAY -> date.plusDays (offset);
            case MONTH -> date.plusMonths (offset);
            case YEAR -> date.plusYears (offset);
            default -> TBSConstants.NOW;
        };
        */
        return date;
    }
    
    private LocalDate getToDate (LocalDate from) {
        return switch (scale) {
            case DAY -> from.plusDays (offset + amount);
            case MONTH -> from.plusMonths (offset + amount);
            case YEAR -> from.plusYears (offset + amount);
            default -> from.plusDays (1);
        };
    }
    
    public Series <String, Number> makeSeries (boolean planned, boolean summary) {
        final var series = new Series <String, Number> ();
        
        series.dataProperty ().bind (Bindings.createObjectBinding (() -> {
            final var list = FXCollections.<Data <String, Number>> observableArrayList ();
            
            double sum = 0.0;
            for (int i = 0; i < credits.size (); i++) {
                final var dateCredit = credits.get (i);
                
                final var date = dateCredit.getDate ().toString ();
                double dateSum = 0.0;
                
                for (final var credit : dateCredit) {                    
                    final var value = credit.isPlanned () == planned ? credit.getCreditValue () : 0.0;
                    dateSum += value;
                }
                
                sum += dateSum;
                
                if (i >= offset) {                    
                    list.add (new Data <> (date, summary ? sum : dateSum));
                }
            }
            
            return list;
        }, credits));
        
        return series;
    }
    
    public ObservableList <ICredit> makeTableRowsList () {
        final var list = FXCollections.<ICredit> observableArrayList ();
        credits.addListener ((ListChangeListener <DateCredit>) change -> {
            list.clear ();
            
            for (int i = offset, rowIndex = 1; i < credits.size (); i++) {
                final var dateCredit = credits.get (i);
                
                for (final var credit : dateCredit) {   
                    final var prop = credit.getProperty (ICredit.INDEX_PROPERTY, () -> 0, false);
                    prop.set (rowIndex++);
                        
                    list.add (credit);
                }
            }
        });
        
        return list;
    }
    
}
