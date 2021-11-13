package ru.shemplo.tbs.entity;

import java.io.Serializable;

import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSUtils;

public interface IPlanningBond extends Serializable, CustomValueHolder <Integer>, ObservableEntity <IPlanningBond> {
    
    public static final String INDEX_PROPERTY = "index";
    public static final String CALC_AMOUNT_PROPERTY = "amount-calculated";
    
    String getCode ();
    
    int getAmount ();
    
    void setAmount (int amount);
    
    default double getPrice () {
        return TBSUtils.aOrB (TBSBondManager.getBondPrice (getCode ()), 0.0);
    }
    
    default double getCalculatedAmount () {
        return TBSUtils.aOrB (getProperty (CALC_AMOUNT_PROPERTY, () -> 0.0, false).get (), 0.0);
    }
    
    default void updateCalculatedAmount (double value) {
        getProperty (CALC_AMOUNT_PROPERTY, () -> 0.0, false).set (value);
    }
    
}
