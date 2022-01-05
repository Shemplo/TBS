package ru.shemplo.tbs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.IPlanningBond;
import ru.shemplo.tbs.entity.PlanningBond;
import ru.shemplo.tbs.entity.PlanningDump;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class TBSPlanner implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
    
    public static final File DUMP_FILE = new File ("plan.bin");
    
    public static void restore () {
        log.info ("Restoring planning bonds from a binary file...");
        if (DUMP_FILE.exists ()) {
            TBSDumpService.getInstance ().restore (
                DUMP_FILE.getName (), 
                PlanningDump.class
            );
        } else {
            getInstance ().updateDistributionParameters (
                DistributionCategory.SUM, 0.0, 0.0
            );
        }
    }
    
    @Getter
    private transient ObservableList <IPlanningBond> bonds = FXCollections.observableArrayList ();
    private transient Map <String, IPlanningBond> ticker2bond = new HashMap <> ();
    
    @Getter
    private transient SimpleDoubleProperty summaryPrice = new SimpleDoubleProperty (0.0);
    
    @Getter
    private transient Property <Number> summaryLots = new SimpleIntegerProperty (0);
    
    @Getter
    private DistributionCategory category;
    
    @Getter
    private double amount, diversification;
    
    @Getter
    private long analyzeDays = 7L;
    
    public synchronized void addBond (String ticker) {
        if (ticker != null && !hasBond (ticker)) {
            final var bond = new PlanningBond (ticker).getProxy ();
            
            ticker2bond.put (ticker, bond);
            bonds.add (bond);
            sortThis (); 
            
            updateDistribution ();
            dump ();
            
            TBSBalanceController.getInstance ().updateBalance ();
        }
    }
    
    public void removeBond (String ticker) {
        if (ticker != null && hasBond (ticker)) {            
            synchronized (ticker2bond) {
                if (hasBond (ticker)) {
                    bonds.removeIf (b -> ticker.equals (b.getCode ()));
                    ticker2bond.remove (ticker);
                    updateIndices ();
                    
                    updateDistribution ();
                    dump ();
                    
                    TBSBalanceController.getInstance ().updateBalance ();
                }
            }
        }
    }
    
    private void sortThis () {
        bonds.sort (Comparator.<IPlanningBond, Double> comparing (
            bond -> TBSBondManager.getBondScore (bond.getCode ())
        ).reversed ());
        
        updateIndices ();
    }
    
    private void updateIndices () {
        for (int i = 0; i < bonds.size (); i++) {
            final var prop = bonds.get (i).getProperty (
                IPlanningBond.INDEX_PROPERTY, () -> 0, false
            );
            
            prop.set (i + 1);
        }
    }
    
    public void updateDistributionParameters (DistributionCategory category, double amount, double diversification) {
        this.category = category; this.amount = amount; this.diversification = diversification;
        updateDistribution ();
        dump ();
        
        TBSBalanceController.getInstance ().updateBalance ();
    }
    
    public void updateDistribution () {
        final var d = 100.0 - diversification;
        final var k = -Math.tan (Math.PI * (d == 100.0 ? 99.9999 : d) / 200.0);
        final var b = bonds.size ();
        
        double sum = 0;
        for (int i = 0; i < b; i++) {
            sum += Math.max (0.0, linearValue (i, k, b));
        }
        
        if (sum != 0.0) {
            double totalPrice = 0.0;
            int totalLots = 0;
            
            for (int i = 0; i < b; i++) {
                final var bond = bonds.get (i);
                
                final var factor = Math.max (0.0, linearValue (i, k, b)) / sum;
                final var price = bond.getRUBPrice ();
                
                if (category == DistributionCategory.LOTS) {
                    bond.setAmount ((int) (factor * amount));
                    bond.updateCalculatedAmount (factor * amount);
                } else if (category == DistributionCategory.SUM && price != 0.0) {
                    bond.setAmount ((int) Math.round (factor * amount / price));
                    bond.updateCalculatedAmount (factor * amount / price);
                } else {
                    bond.updateCalculatedAmount (0.0);
                    bond.setAmount (0);
                }
                
                totalPrice += price * bond.getCurrentValue ();
                totalLots += bond.getCurrentValue ();
            }
            
            summaryPrice.setValue (totalPrice);
            summaryLots.setValue (totalLots);
        }
    }
    
    private double linearValue (double x, double k, double b) {
        return k * x + b;
    }
    
    public void updateRecommendationsParameter (long days) {
        analyzeDays = days;
        dump ();
    }
    
    public void dump () {
        TBSDumpService.getInstance ().dump (new PlanningDump (this), DUMP_FILE.getName ());
    }
    
    public boolean hasBond (String ticker) {
        return ticker2bond.containsKey (ticker);
    }
    
    public IPlanningBond getBondByTicker (String ticker) {
        return ticker2bond.get (ticker);
    }
    
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        instance = this;
        
        summaryPrice = new SimpleDoubleProperty (0.0);
        summaryLots = new SimpleIntegerProperty (0);
        
        bonds = FXCollections.observableArrayList ();
        TBSUtils.doIfNN (_serializeBonds, bonds -> {
            bonds.forEach (bond -> {
                this.bonds.add (bond.getProxy ());
            });
            
            _serializeBonds.clear ();
        });
        
        ticker2bond = new HashMap <> ();
        for (final var bond : bonds) {
            ticker2bond.put (bond.getCode (), bond);
        }
        
        sortThis ();
        updateDistribution ();
        
        TBSBalanceController.getInstance ().updateBalance ();
    }
    
    private List <IPlanningBond> _serializeBonds;
    
    private void writeObject (ObjectOutputStream out) throws IOException, ClassNotFoundException {
        _serializeBonds = bonds.stream ().map (IPlanningBond::getRealObject)
                        . collect (Collectors.toList ());
        out.defaultWriteObject ();
    }
    
    public static enum DistributionCategory {
        
        SUM, LOTS;
        
    }
    
}
