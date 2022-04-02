package ru.shemplo.tbs.moex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MOEXRequests {
    
    private static final String ISS_DOMAIN = "https://iss.moex.com";
    private static final String MOEX_DOMAIN = "https://www.moex.com";
    
    public static URL makeBondDescriptionURLForMOEX (String ticker) {
        try {
            return new URL (String.format ("%s/iss/securities/%s.xml", ISS_DOMAIN, ticker));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static URL makeBondCouponsURLForMOEX (String ticker) {
        try {
            return new URL (String.format (
                "%s/iss/statistics/engines/stock/markets/bonds/bondization/%s.xml"
                    + "?iss.meta=off&coupons.columns=coupondate,recorddate,value&limit=unlimited", 
                ISS_DOMAIN, ticker
            ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static URL makeBondLastPriceURLForMOEX (String board, String ticker) {
        try {
            return new URL (String.format (
                "%s/iss/engines/stock/markets/bonds/boards/%s/securities/%s.xml"
                    + "?iss.meta=off&securities.columns=ACCRUEDINT&marketdata.columns=SECID,LAST,MARKETPRICE,LCURRENTPRICE", 
                ISS_DOMAIN, "TQCB", ticker
            ));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public static URL makeMOEXBondPageURL (String ticker) {
        try {
            return new URL (String.format ("%s/ru/issue.aspx?code=%s&utm_source=www.moex.com", MOEX_DOMAIN, ticker));
        } catch (MalformedURLException murle) {
            return null;
        }
    }
    
    public static String loadBondPageContent (String ticker) throws IOException {
        final var URL = makeMOEXBondPageURL (ticker);
        log.debug ("Sending request for bond page in MOEX: " + URL);
        
        try (final var connection = URL.openConnection ().getInputStream ()) {            
            return new String (connection.readAllBytes ());
        }
    }
    
    public static String loadEmitterPageContent (String URI) throws IOException {
        final var URL = new URL (String.format ("%s/%s", MOEX_DOMAIN, URI));
        log.debug ("Sending request for emitter page in MOEX: " + URL);
        
        try (final var connection = URL.openConnection ().getInputStream ()) {            
            return new String (connection.readAllBytes ());
        }
    }
    
}
