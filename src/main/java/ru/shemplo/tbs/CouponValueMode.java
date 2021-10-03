package ru.shemplo.tbs;

public enum CouponValueMode {
    
    FIXED, NOT_FIXED, UNDEFINED
    
    ;
    
    public static CouponValueMode fromValue (String text) {
        for (final var mode : values ()) {
            if (mode.name ().equals (text)) {
                return mode;
            }
        }
        return null;
    }
    
}
