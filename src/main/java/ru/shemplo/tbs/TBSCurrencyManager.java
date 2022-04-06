package ru.shemplo.tbs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.shemplo.tbs.entity.Currency;
import ru.shemplo.tbs.entity.IProfile;

@NoArgsConstructor (access = AccessLevel.PRIVATE)
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
    
    public void initialize (IProfile profile, TBSLogWrapper log) {
        try {
            final var client = TBSClient.getInstance ().getConnection (profile, log);
            
            log.info ("Loading current currency quotes from Tinkoff...");
            //client.getInstrumentsService ().getAllCurrenciesSync ().get (0).getF
            //client.getMarketDataService ().getCandlesSync (null, null, null, null);
            currency2coefficient = client.getInstrumentsService ().getAllCurrenciesSync ().stream ()
                . map (cur -> {
                    final var currency = TBSUtils.getCurrencyByTicker (cur.getTicker ());
                    if (currency.isEmpty ()) { return null; }
                    
                    //final var now = Instant.now ();
                    final var price = client.getMarketDataService ().getLastPricesSync (List.of (cur.getFigi ())).get (0).getPrice ();
                    final var coeff = Double.parseDouble (price.getUnits () + "." + price.getNano ());
                    /*
                    final var coeff = client.getMarketDataService ().getCandlesSync (
                        cur.getFigi (), now.minus (3, ChronoUnit.DAYS), now, 
                        CandleInterval.CANDLE_INTERVAL_DAY
                    ).stream ().reduce ((acc, candle) -> {
                        return candle.getTime ().getSeconds () > acc.getTime ().getSeconds () ? candle : acc;
                    }).map (HistoricCandle::getClose)
                    . map (q -> Double.parseDouble (q.getUnits () + "." + q.getNano ()))
                    . orElse (1.0);
                    */
                    
                    return Map.entry (currency.get (), coeff);
                })
                . filter (Objects::nonNull)
                . collect (Collectors.toMap (Entry::getKey, Entry::getValue));
        } catch (IOException ioe) {
            log.error ("Failed to load currencies", ioe);
            currency2coefficient = Map.of ();
        }
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
