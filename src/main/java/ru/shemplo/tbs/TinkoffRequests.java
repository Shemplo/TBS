package ru.shemplo.tbs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TinkoffRequests {
    
    private static final String TINKOFF_DOMAIN = "https://www.tinkoff.ru";
    
    public static URL makeTinkoffBondPageURL (String ticker) {
        try {
            return new URL (String.format ("%s/invest/bonds/%s/", TINKOFF_DOMAIN, ticker));
        } catch (MalformedURLException murle) {
            return null;
        }
    }
    
    public static String loadBondPageContent (String ticker) throws IOException {
        final var URL = makeTinkoffBondPageURL (ticker);
        log.debug ("Sending request for bond page in Tinkoff: " + URL);
        
        try (final var connection = URL.openConnection ().getInputStream ()) {            
            return new String (connection.readAllBytes ());
        }
    }
    
}
