package ru.shemplo.tbs;

import java.io.Serializable;

public class TBSPlanner implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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
