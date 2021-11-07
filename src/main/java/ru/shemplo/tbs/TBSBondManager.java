package ru.shemplo.tbs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;

public class TBSBondManager implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static volatile TBSBondManager instance;
    
    public static TBSBondManager getInstance () {
        if (instance == null) {
            synchronized (TBSBondManager.class) {
                if (instance == null) {
                    instance = new TBSBondManager ();
                }
            }
        }
        
        return instance;
    }
    
    public static String getBondName (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getName, "");
    }
    
    public static double getBondScore (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, true), Bond::getScore, 0.0);
    }
    
    public static Double getBondPrice (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getLastPrice, null);
    }
    
    public static LocalDate getBondNextCoupon (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getNextCoupon, null);
    }
    
    public static int getBondLots (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getLots, 0);
    }
    
    public static Bond getBondByTicker (String ticker, boolean scannedPreferred) {
        final var portfolio = getInstance ().ticker2portfolio.get (ticker);
        final var scanned = getInstance ().ticker2scanned.get (ticker);
        
        return scannedPreferred ? TBSUtils.aOrB (scanned, portfolio) : TBSUtils.aOrB (portfolio, scanned);
    }
    
    private List <Bond> scanned;
    private List <Bond> portfolio;
    
    private transient Map <String, Bond> ticker2portfolio = new HashMap <> ();
    private transient Map <String, Bond> ticker2scanned = new HashMap <> ();
    
    public void initialize (ITBSProfile profile, OpenApi client, Logger log) {
        log.info ("Loading bonds from portfolio (with data from Tinkoff and MOEX)...");
        portfolio = client.getUserContext ().getAccounts ().join ().getAccounts ().parallelStream ()
            . flatMap (acc -> {
                return client.getPortfolioContext ().getPortfolio (acc.getBrokerAccountId ()).join ()
                     . getPositions ().stream ();
            })
            . filter (pos -> pos.getInstrumentType () == InstrumentType.BOND)
            . map (Bond::new).collect (Collectors.toList ());
        
        log.info ("Loading data abount bonds from Tinkoff and MOEX...");
        scanned = client.getMarketContext ().getMarketBonds ().join ().getInstruments ().stream ()
            . filter (instrument -> profile.getCurrencies ().contains (instrument.getCurrency ())).parallel ()
            . map (Bond::new).filter (profile::testBond)//.limit (profile.getMaxResults ())
            . collect (Collectors.toList ());
        
        updateMapping ();
    }
    
    public void analize (ITBSProfile profile) {
        portfolio.forEach (bond -> {
            bond.updateScore (profile);
        });
        
        scanned.forEach (bond -> {
            final var sameBond = getBondByTicker (bond.getCode (), false);
            bond.setLots (TBSUtils.mapIfNN (sameBond, Bond::getLots, 0));
            bond.updateScore (profile);
        });
        
        portfolio.sort (Comparator.comparing (Bond::getLots).reversed ());
        scanned.sort (Comparator.comparing (Bond::getScore).reversed ());
    }
    
    public List <Bond> getScanned () {
        return Collections.unmodifiableList (scanned);
    }
    
    public List <Bond> getPortfolio () {
        return Collections.unmodifiableList (portfolio);
    }
    
    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject ();
        instance = this;
        
        updateMapping ();
    }
    
    private void updateMapping () {
        if (ticker2portfolio == null) {
            ticker2portfolio = new HashMap <> ();
        }
        
        for (final var bond : portfolio) { 
            ticker2portfolio.put (bond.getCode (), bond);
        }
        
        if (ticker2scanned == null) {
            ticker2scanned = new HashMap <> ();
        }
        
        for (final var bond : scanned) {
            ticker2scanned.put (bond.getCode (), bond);
        }
    }
    
}
