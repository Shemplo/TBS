package ru.shemplo.tbs.entity;

import java.util.function.Supplier;

import javafx.beans.property.ObjectProperty;

public interface ObservableEntity <T> {
    
    T getProxy ();
    
    <V> ObjectProperty <V> getProperty (String field, Supplier <V> defaultValue, boolean applyReadWrite);
    
    default <V> ObjectProperty <V> getRWProperty (String field, Supplier <V> defaultValue) {
        return getProperty (field, defaultValue, true);
    }
    
    default T getRealObject () {
        return null;
    }
    
}
