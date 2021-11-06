package ru.shemplo.tbs.entity;

import java.io.Serializable;

public interface IBond extends Serializable, ObservableEntity <IBond> {
    
    public static final String UI_SELECTED_PROPERTY = "ui-selected";
    
    CouponValueMode getCouponValuesMode ();
    
    long getYearsToEnd ();
    
    long getMonthsToEnd ();
    
}
