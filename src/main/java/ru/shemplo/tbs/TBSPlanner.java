package ru.shemplo.tbs;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import ru.shemplo.tbs.entity.PlanDump;
import ru.shemplo.tbs.entity.PlanningBond;

public class TBSPlanner implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final File DUMP_FILE = new File ("plan.bin");
    
    private static volatile TBSPlanner instance;
    
    public static TBSPlanner getInstance () {
        if (instance == null) {
            synchronized (TBSPlanner.class) {
                if (instance == null) {
                    instance = new TBSPlanner ();
                }
            }
        }
        
        return instance;
    }
    
    @Getter
    private ObservableList <PlanningBond> bonds = FXCollections.observableArrayList ();
    private transient Set <String> tickers = new HashSet <> ();
    
    @Getter
    private DistributionCategory category;
    
    @Getter
    private double amount, diversification;
    
    /**
     * Call this method only after deserialization of this object
     */
    public void updatePool (PlanDump dump) {
        if (tickers == null) {
            tickers = new HashSet <> ();
        }
        
        bonds.addAll (dump.getBonds ());
        sortThis ();
        
        for (final var bond : bonds) {
            tickers.add (bond.getCode ());
        }
        
        updateParameters (dump.getCategory (), dump.getAmount (), dump.getDiversification ());
    }
    
    public void addBond (String ticker, double score, double price) {
        if (ticker != null && tickers.add (ticker)) {
            bonds.add (new PlanningBond (ticker, score, price));
            sortThis (); 
            
            updateDistribution ();
        }
    }
    
    public void removeBond (String ticker) {
        if (ticker != null && hasBond (ticker)) {            
            synchronized (tickers) {
                if (tickers.remove (ticker)) {
                    bonds.removeIf (b -> ticker.equals (b.getCode ()));
                    updateDistribution ();
                }
            }
        }
    }
    
    private void sortThis () {
        bonds.sort (Comparator.comparing (PlanningBond::getScore).reversed ());
    }
    
    public void updateParameters (DistributionCategory category, double amount, double diversification) {
        this.category = category; this.amount = amount; this.diversification = diversification;
        updateDistribution ();
        dump ();
    }
    
    public void updateDistribution () {
        final var d = 100.0 - diversification;
        final var k = -Math.tan (Math.PI * (d == 100.0 ? 99.9999 : d) / 200.0);
        final var b = bonds.size ();
        
        double sum = 0;
        for (int i = 0; i < b; i++) {
            sum += Math.max (0.0, linearValue (i, k, b));
        }
        
        //System.out.println (String.format ("y = %.4f * x + %d", k, b)); // SYSOUT
        //System.out.println (String.format ("Sum: %.4f, min: %.4f, max: %.4f", sum, min, max)); // SYSOUT
        if (sum != 0.0) {
            for (int i = 0; i < b; i++) {
                final var bond = bonds.get (i);
                
                final var factor = Math.max (0.0, linearValue (i, k, b)) / sum;
                //System.out.println (String.format ("%.4f => %.4f | %.4f", factor, factor / sum, factor / max)); // SYSOUT
                if (category == DistributionCategory.LOTS) {
                    bond.setAmount ((int) (factor * amount));
                    bond.setIdealAmount (factor * amount);
                } else if (category == DistributionCategory.SUM) {
                    bond.setAmount ((int) (factor * amount / bond.getPrice ()));
                    bond.setIdealAmount (factor * amount / bond.getPrice ());
                } else {
                    bond.setIdealAmount (0.0);
                    bond.setAmount (0);
                }
            }
        }
    }
    
    private double linearValue (double x, double k, double b) {
        return k * x + b;
    }
    
    public void dump () {
        TBSDumpService.getInstance ().dump (
            new PlanDump (List.copyOf (bonds), category, amount, diversification), 
            DUMP_FILE.getName ()
        );
    }
    
    public boolean hasBond (String ticker) {
        return tickers.contains (ticker);
    }
    
    public static enum DistributionCategory {
        
        SUM, LOTS;
        
    }
    
}
