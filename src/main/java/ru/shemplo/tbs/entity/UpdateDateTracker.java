package ru.shemplo.tbs.entity;

import java.util.Date;

public interface UpdateDateTracker {
    
    void setUpdated (Date date);
    
    Date getUpdated ();
    
    default void updateNow () {
        setUpdated (new Date ());
    }
    
}
