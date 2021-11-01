package ru.shemplo.tbs;

import java.io.Serializable;

import lombok.Setter;

public class TBSPlanner implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Setter
    private static volatile TBSPlanner instance;
    
    public static TBSPlanner getInstance () {
        if (instance == null) {
            synchronized (TBSPlanner.class) {
                if (instance == null) {
                    instance = new TBSPlanner ();
                }
            }
        }
        
        return instance;
    }
    
}
