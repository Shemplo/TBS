package ru.shemplo.tbs.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public interface IBond extends Serializable, ObservableEntity <IBond> {
    
    public static final String UI_SELECTED_PROPERTY = "ui-selected";
    
    String getName ();
    
    String getCode ();
    
    String getFigi ();
    
    LocalDate getNow ();
    
    LocalDate getEnd ();
    
    List <? extends ICoupon> getCoupons ();
    
    CouponValueMode getCouponValuesMode ();
    
    long getYearsToEnd ();
    
    long getMonthsToEnd ();
    
}
