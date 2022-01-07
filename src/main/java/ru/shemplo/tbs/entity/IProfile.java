package ru.shemplo.tbs.entity;

import java.io.Serializable;
import java.util.Set;

import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.TBSUtils;
import ru.tinkoff.invest.openapi.model.rest.Currency;

public interface IProfile extends Serializable {
    
    String name ();
    
    default void setName (String name) {
        throw new UnsupportedOperationException ();
    }
    
    String getToken ();
    
    default void setToken (String token) {
        throw new UnsupportedOperationException ();
    }
    
    boolean isHighResponsible ();
    
    default void setHighResponsible (boolean hr) {
        throw new UnsupportedOperationException ();
    }
    
    long getMaxResults ();
    
    default void setMaxResults (long maxResults) {
        throw new UnsupportedOperationException ();
    }
    
    double getInflation ();
    
    default void setInflation (double inflation) {
        throw new UnsupportedOperationException ();
    }
    
    Range <Integer> getMonthsTillEnd ();
    
    default void setMonthsTillEnd (Range <Integer> mte) {
        throw new UnsupportedOperationException ();
    }
    
    Range <Integer> getCouponsPerYear ();
    
    default void setCouponsPerYear (Range <Integer> cpy) {
        throw new UnsupportedOperationException ();
    }
    
    Range <Integer> getDaysToCoupon ();
    
    default void setDaysToCoupon (Range <Integer> dtc) {
        throw new UnsupportedOperationException ();
    }
    
    Range <Double> getNominalValue (); 
    
    default void setNominalValue (Range <Double> nv) {
        throw new UnsupportedOperationException ();
    }
    
    Range <Double> getPercentage ();
    
    default void setPercentage (Range <Double> p) {
        throw new UnsupportedOperationException ();
    }
    
    Range <Double> getPrice ();
    
    default void setPrice (Range <Double> p) {
        throw new UnsupportedOperationException ();
    }
    
    Set <Currency> getCurrencies ();
    
    default void setCurrencies (Set <Currency> currencies) {
        throw new UnsupportedOperationException ();
    }
    
    Set <BondCreditRating> getCreditRatings ();
    
    default void setCreditRatings (Set <BondCreditRating> creditRatings) {
        throw new UnsupportedOperationException ();
    }
    
    Set <CouponValueMode> getCouponValuesModes ();
    
    default void setCouponValuesModes (Set <CouponValueMode> cvm) {
        throw new UnsupportedOperationException ();
    }
    
    Set <Long> getBannedEmitters ();
    
    boolean isEditable ();
    
    default long getSafeMinMonths () {
        return TBSUtils.mapIfNN (getMonthsTillEnd (), Range::getMin, 0);
    }
    
    default double getSafeMaxPrice (double bondLastPrice) {
        return TBSUtils.mapIfNN (getPrice (), Range::getMax, bondLastPrice * 1.0);
    }
    
    default long getSafeMaxDaysToCoupon () {
        return TBSUtils.mapIfNN (getDaysToCoupon (), Range::getMax, 0);
    }
    
    default boolean testBond (Bond bond) {
        final var currencyCoeff = TBSCurrencyManager.getInstance ().getToRubCoefficient (bond.getCurrency ());
        final var creditRating = TBSUtils.mapIfNN (bond, 
            b -> TBSEmitterManager.getCreditRating (b.getEmitterId ()), 
            BondCreditRating.UNDEFINED
        );
        
        return debugConditions (String.valueOf (bond)) 
            && bond != null && getCurrencies ().contains (bond.getCurrency ()) 
            && debugConditions ("    Bond and currency passed")
            && getCreditRatings ().contains (creditRating) 
            && debugConditions ("    Emitter credit rating passed")
            && getCouponValuesModes ().contains (bond.getCouponValuesMode ()) 
            && debugConditions ("    Coupons mode passed")
            && !getBannedEmitters ().contains (bond.getEmitterId ()) 
            && debugConditions ("    Banned emitters passed")
            && TBSUtils.mapIfNN (getDaysToCoupon (), r -> r.isBetween ((int) bond.getDaysToCoupon ()), true)
            && debugConditions ("    Max days to coupon passed")
            && TBSUtils.mapIfNN (getCouponsPerYear (), r -> r.isBetween ((int) bond.getCouponsPerYear ()), true)
            && debugConditions ("    Coupons per year passed")
            && TBSUtils.mapIfNN (getMonthsTillEnd (), r -> r.isBetween ((int) bond.getMonthsToEnd ()), true)
            && debugConditions ("    Month till end passed")
            && TBSUtils.mapIfNN (getPercentage (), r -> r.isBetween (bond.getPercentage ()), true)
            && debugConditions ("    Min percentage passed")
            && TBSUtils.mapIfNN (getPrice (), r -> r.isBetween (bond.getLastPrice () * currencyCoeff), true)
            && debugConditions ("    Max price passed")
            && TBSUtils.mapIfNN (getNominalValue (), r -> r.isBetween (bond.getNominalValue () * currencyCoeff), true)
            && debugConditions ("    Nominal value passed")
            && debugConditions ("All challenges passed"); 
    }
    
    default boolean debugConditions (String message) {
        //System.out.println (message); // SYSOUT
        return true;
    }
    
    default String getProfileDescription () {
        return String.format (
            "Mode: %s,  Max results: %d,  Inflation: %.1f%%,  Months: %s,  Coupons per year: %s,  Days to coupon: %s,  "
            + "Nominal: %s (RUB),  MOEX %%: %s,  Price: %s (RUB),  Currencies: %s,  Coupon modes: %s", 
            isHighResponsible () ? "Production" : "Sandbox", getMaxResults (), getInflation () * 100, getMonthsTillEnd (), 
            getCouponsPerYear (), getDaysToCoupon (), getNominalValue (), getPercentage (), getPrice (), getCurrencies (), 
            getCouponValuesModes ()
        );
    }
    
    default String getShortProfileDescription () {
        return String.format (
            "%10s    MR: %-4d      I: %-10.2f          Ms: %-10s    CPY: %-10s%n"
            + "DTC: %-10s       N (RUB): %-10s     P (RUB): %-10s     P%%: %-10s%nC: %s  CM: %s", 
            isHighResponsible () ? "Production" : "Sandbox", getMaxResults (), getInflation () * 100, getMonthsTillEnd (), 
            getCouponsPerYear (), getDaysToCoupon (), getNominalValue (), getPrice (), getPercentage (), getCurrencies (), 
            getCouponValuesModes ()
        );
    }
    
    IProfile copy ();
    
}
