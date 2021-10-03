package ru.shemplo.tbs;

import java.io.Serializable;
import java.util.Set;

import ru.tinkoff.invest.openapi.model.rest.Currency;

public interface ITBSProfile extends Serializable {
    
    String name ();
    
    String getTokenFilename ();
    
    boolean isHighResponsible ();
    
    long getMaxResults ();
    
    double getInflation ();
    
    Long getMonthsTillEnd ();
    
    Long getCouponsPerYear ();
    
    Long getMaxDaysToCoupon ();
    
    Double getNominalValue (); 
    
    Double getMinPercentage ();
    
    Double getMaxPrice ();
    
    Set <Currency> getCurrencies ();
    
    Set <CouponValueMode> getCouponValuesModes ();
    
    Set <Long> getBannedEmitters ();
    
    default long getSafeMinMonths () {
        return getMonthsTillEnd () == null ? 0 : getMonthsTillEnd ();
    }
    
    default double getSafeMaxPrice (double bondLastPrice) {
        return getMaxPrice () == null ? bondLastPrice * 1.1 : getMaxPrice ();
    }
    
    default long getSafeMaxDaysToCoupon () {
        return getMaxDaysToCoupon () == null ? 0 : getMaxDaysToCoupon ();
    }
    
    default boolean testBond (Bond bond) {
        return bond != null && getCurrencies ().contains (bond.getCurrency ())
            && getCouponValuesModes ().contains (bond.getCouponValuesMode ())
            && !getBannedEmitters ().contains (bond.getEmitterId ())
            && (getMaxDaysToCoupon () == null ? true : bond.getDaysToCoupon () <= getMaxDaysToCoupon ())
            && (getCouponsPerYear () == null ? true : bond.getCouponsPerYear () >= getCouponsPerYear ())
            && (getMonthsTillEnd () == null ? true : bond.getMonthToEnd () >= getMonthsTillEnd ())
            && (getMinPercentage () == null ? true : bond.getPercentage () >= getMinPercentage ())
            && (getMaxPrice () == null ? true : bond.getLastPrice () <= getMaxPrice ())
            && (getNominalValue () == null ? true : bond.getNominalValue () >= getNominalValue ());
    }
    
    default String getProfileDescription () {
        return String.format (
            "Name: %s,  Max results: %d,  Inflation: %.1f%%,  Months: %d [↥],  C / Y: %d [↥],  Days to C: %d [↧],"
            + "  Nominal: %.1f [↥],  MOEX %%: %.1f [↥],  Price: %.1f [↧],  Currencies: %s,  C modes: %s", 
            name (), getMaxResults (), getInflation () * 100, getMonthsTillEnd (), getCouponsPerYear (), 
            getMaxDaysToCoupon (), getNominalValue (), getMinPercentage (), getMaxPrice (), getCurrencies (), 
            getCouponValuesModes ()
        );
    }
    
}
