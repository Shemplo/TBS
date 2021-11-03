package ru.shemplo.tbs.entity;


public interface CustomValueHolder <T> {
    
    T getCustomValue ();
    
    T getCurrentValue ();
    
    void setCustomValue (T value);
    
}
