package ru.shemplo.tbs.entity;


public enum Currency {
    
    RUB, USD, EUR, GBP, CHF,
    
    UNKNOWN
    
    ;
    
    public static Currency from (java.util.Currency currency) {
        return from (currency.getCurrencyCode ());
    }
    
    public static Currency from (String code) {
        return Currency.valueOf (code.toUpperCase ());
    }
    
}
