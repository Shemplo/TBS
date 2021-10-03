package ru.shemplo.tbs.moex;

import java.net.MalformedURLException;
import java.net.URL;

public class MOEXRequests {
    
    public static URL makeBondDescriptionURLForMOEX (String ticker) {
        try {
            return new URL (String.format ("https://iss.moex.com/iss/securities/%s.xml", ticker));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static URL makeBondCouponsURLForMOEX (String ticker) {
        try {
            return new URL (String.format (
            "https://iss.moex.com/iss/statistics/engines/stock/markets/bonds/bondization/%s.xml"
                + "?iss.meta=off&coupons.columns=coupondate,value&limit=unlimited", 
                ticker
            ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static URL makeBondLastPriceURLForMOEX (String board, String ticker) {
        try {
            return new URL (String.format (
                "https://iss.moex.com/iss/engines/stock/markets/bonds/boards/%s/securities/%s.xml"
                        + "?iss.meta=off&iss.only=marketdata&marketdata.columns=SECID,LAST", 
                "TQCB", ticker
            ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
}
