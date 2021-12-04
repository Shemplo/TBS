package ru.shemplo.tbs.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Range <N extends Number> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final N min, max;
    
    public boolean isBetween (N value) {
        return isBetween (value, true, true);
    }
    
    public boolean isBetween (N value, boolean inclusiveLeft, boolean inclusiveRight) {
        final var dvalue = value.doubleValue ();
        return (min == null || (inclusiveLeft ? min.doubleValue () <= dvalue : min.doubleValue () < dvalue))
            && (max == null || (inclusiveRight ? max.doubleValue () >= dvalue : max.doubleValue () > dvalue));
    }
    
    public Range <N> copy () {
        return new Range <> (min, max);
    }
    
    @Override
    public String toString () {
        if (min == null && max == null) {
            return "any";
        } else if (min == null && max != null) {
            return max + " [↧]";
        } else if (min != null && max == null) {
            return min + " [↥]";
        } else {
            return String.format ("[%s; %s]", String.valueOf (min), String.valueOf (max));
        }
    }
    
}
