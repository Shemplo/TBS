package ru.shemplo.tbs;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import lombok.Getter;
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
    
    @Getter
    private List <Bond> scanned;
    
    @Getter
    private List <Bond> portfolio;
    
    private transient Map <String, Bond> ticker2bond = new HashMap <> ();
    
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
            . map (Bond::new).filter (profile::testBond).limit (profile.getMaxResults ())
            . collect (Collectors.toList ());
        
        updateMapping ();
    }
    
    /**
     * Call this method only after deserialization of this object
     */
    public void updateMapping () {
        for (final var bond : scanned) {
            ticker2bond.put (bond.getCode (), bond);
        }
        
        // Portfolio goes after scanned to override scanned if it exists 
        // (Because portfolio instance has information about lots)
        for (final var bond : portfolio) { 
            ticker2bond.put (bond.getCode (), bond);
        }
    }
    
    public void analize (ITBSProfile profile) {
        portfolio.forEach (bond -> {
            bond.updateScore (profile);
        });
        
        scanned.forEach (bond -> {
            final var sameBond = getBondByTicker (bond.getCode ());
            bond.setLots (TBSUtils.mapIfNN (sameBond, Bond::getLots, 0));
            bond.updateScore (profile);
        });
        
        portfolio.sort (Comparator.comparing (Bond::getLots).reversed ());
        scanned.sort (Comparator.comparing (Bond::getScore).reversed ());
    }
    
    public Bond getBondByTicker (String ticker) {
        return ticker2bond.get (ticker);
    }
    
}
