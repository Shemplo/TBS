package ru.shemplo.tbs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import ru.shemplo.tbs.entity.ITBSProfile;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.Candle;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;

public class TBSCurrencyManager implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static volatile TBSCurrencyManager instance;
    
    public static TBSCurrencyManager getInstance () {
        if (instance == null) {
            synchronized (TBSCurrencyManager.class) {
                if (instance == null) {
                    instance = new TBSCurrencyManager ();
                }
            }
        }
        
        return instance;
    }
    
    private Map <Currency, Double> currency2coefficient;
    
    public void initialize (ITBSProfile profile, OpenApi client, Logger log) {
        log.info ("Loading current currency quotes from Tinkoff...");
        currency2coefficient = client.getMarketContext ().getMarketCurrencies ().join ().getInstruments ().stream ()
            . map (cur -> {
                final var currency = TBSUtils.getCurrencyByTicker (cur.getTicker ());
                if (currency.isEmpty ()) { return null; }
                
                final var now = OffsetDateTime.now ();
                final var coeff = client.getMarketContext ().getMarketCandles (
                    cur.getFigi (), now.minusDays (3), now, CandleResolution.DAY
                ).join ().flatMap (res -> res.getCandles ().stream ().reduce ((acc, candle) -> {
                    return candle.getTime ().isAfter (acc.getTime ()) ? candle : acc;
                })).map (Candle::getC).orElse (BigDecimal.ONE).doubleValue ();
                
                return Map.entry (currency.get (), coeff);
            })
            . filter (Objects::nonNull)
            . collect (Collectors.toMap (Entry::getKey, Entry::getValue));
    }
    
    public String getStringQuotes () {
        return TBSUtils.mapIfNN (currency2coefficient, Map::toString, "<not defined>");
    }
    
    public double getToRubCoefficient (Currency currency) {
        return TBSUtils.mapIf2NN (currency2coefficient, currency, (m, c) -> m.get (c), 1.0);
    }
    
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        instance = this;
    }
    
}
