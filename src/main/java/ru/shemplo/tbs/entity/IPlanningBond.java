package ru.shemplo.tbs.entity;

import java.io.Serializable;

import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSUtils;

public interface IPlanningBond extends Serializable, CustomValueHolder <Long>, ObservableEntity <IPlanningBond> {
    
    public static final String INDEX_PROPERTY = "index";
    public static final String CALC_AMOUNT_PROPERTY = "amount-calculated";
    
    String getCode ();
    
    long getAmount ();
    
    void setAmount (long amount);
    
    void setRecommendedPrice (double price);
    
    double getRecommendedPrice ();
    
    default String getFIGI () {
        return TBSUtils.mapIfNN (TBSBondManager.getBondByTicker (getCode (), true), Bond::getFigi, null);
    }
    
    default double getPrice () {
        return TBSUtils.aOrB (TBSBondManager.getBondPrice (getCode ()), 0.0);
    }
    
    default double getAccCouponIncome () {
        return TBSUtils.aOrB (TBSBondManager.getBondAccCouponIncome (getCode ()), 0.0);
    }
    
    default double getRUBPrice (boolean real) {
        final var currency = TBSBondManager.getBondCurrency (getCode ());
        final var cur2rub = TBSCurrencyManager.getInstance ().getToRubCoefficient (currency);
        return TBSUtils.mapIfNN (real ? getPrice () : getRecommendedPrice (), p -> p * cur2rub, 0.0);
    }
    
    default double getCalculatedAmount () {
        return TBSUtils.aOrB (getProperty (CALC_AMOUNT_PROPERTY, () -> 0.0, false).get (), 0.0);
    }
    
    default void updateCalculatedAmount (double value) {
        getProperty (CALC_AMOUNT_PROPERTY, () -> 0.0, false).set (value);
    }
    
}
