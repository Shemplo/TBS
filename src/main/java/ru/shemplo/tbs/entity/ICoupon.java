package ru.shemplo.tbs.entity;

import java.io.Serializable;
import java.time.LocalDate;

public interface ICoupon extends Serializable, ObservableEntity <ICoupon> {
    
    double getCredit (ITBSProfile profile, LocalDate forDate, LocalDate end);
    
}
