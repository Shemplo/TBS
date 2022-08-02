package ru.shemplo.tbs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import lombok.NonNull;

public class RollingAverage {
    
    private final int window;
    
    private final boolean forward;
    
    public RollingAverage (int window, boolean forward) {
        this.forward = forward;
        this.window = window; 
    }
    
    private int finished;
    
    private Queue <Double> buffer = new LinkedList <> ();
    private List <Double> average = new ArrayList <> ();
    
    private double sum = 0;
    
    public RollingAverage addValue (Double value) {
        value = value == null ? 0 : value;
        
        while (finished > 0) {
            average.remove (average.size () - 1);
            finished--;
        }
        
        buffer.add (value);
        if (!forward) {
            sum += value;            
        }
        
        while (buffer.size () > window) { // actually `if` is enough
            final var tmp = buffer.poll ();
            if (forward) {
                if (finished > 0) {
                    average.set (average.size () - finished, sum / window);
                } else {
                    average.add (sum / window);
                }
            }
            
            sum -= tmp;
        }
        
        if (!forward) {            
            average.add (sum / Math.min (window, buffer.size ()));
        } else { // forward
            sum += value;
        }
        
        return this;
    }
    
    public RollingAverage addSequence (@NonNull Iterable <Double> values) {
        values.forEach (this::addValue);
        return this;
    }
    
    public List <Double> getSequence () {
        return List.copyOf (average);
    }
    
    public List <Double> finish () {
        if (!forward || finished > 0) { return getSequence (); }
        
        Double [] rest = buffer.toArray (Double []::new);
        int tmpWindow = rest.length;
        double tmpSum = sum;
        
        for (int i = 0; i < rest.length; i++, tmpWindow--) {            
            average.add (tmpSum / tmpWindow);
            tmpSum -= rest [i];
        }
        
        finished = rest.length;
        return getSequence ();
    }
    
}
