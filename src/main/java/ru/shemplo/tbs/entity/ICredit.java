package ru.shemplo.tbs.entity;

import java.io.Serializable;
import java.time.LocalDate;

public interface ICredit extends Serializable, ObservableEntity <ICredit> {
    
    public static final String INDEX_PROPERTY = "index";
    
    LocalDate getCreditDate ();
    
    double getCreditValue ();
    
    boolean isPlanned ();
    
}
