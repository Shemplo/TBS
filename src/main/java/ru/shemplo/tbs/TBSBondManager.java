package ru.shemplo.tbs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.BondsDump;
import ru.shemplo.tbs.entity.Currency;
import ru.shemplo.tbs.entity.IProfile;

@Slf4j
@NoArgsConstructor (access = AccessLevel.PRIVATE)
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
    
    public static final File DUMP_FILE = new File ("dump.bin");
    
    public static Date getDumpDate () {
        if (DUMP_FILE.exists ()) {
            return new Date (DUMP_FILE.lastModified ());
        }
        
        return null;
    }
    
    
    public static IProfile restore () {
        log.info ("Restoring bonds from a binary file...");
        final var dump = TBSDumpService.getInstance ().<BondsDump> restore (
            TBSBondManager.DUMP_FILE.getName ()
        );
        
        return TBSUtils.mapIfNN (dump, BondsDump::getProfile, null);
    }
    
    public void dump (IProfile profile) {
        final var currencyManager = TBSCurrencyManager.getInstance ();
        final var dump = new BondsDump (profile, currencyManager, this);
        
        TBSDumpService.getInstance ().dump (dump, TBSBondManager.DUMP_FILE.getName ());
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
    
    public static Double getBondNominal (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getNominalValue, null);
    }
    
    public static Double getBondAccCouponIncome (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getAccCouponIncome, null);
    }
    
    public static Currency getBondCurrency (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getCurrency, null);
    }
    
    public static LocalDate getBondNextCoupon (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getNextCoupon, null);
    }
    
    public static LocalDate getBondNextRecord (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getNextRecord, null);
    }
    
    public static long getBondLots (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), Bond::getLots, 0L);
    }
    
    public static BondCreditRating getBondCreditRating (String ticker) {
        return TBSUtils.map2IfNN (ticker, t -> getBondByTicker (t, false), 
            b -> TBSEmitterManager.getCreditRating (b.getEmitterId ()), 
            BondCreditRating.UNDEFINED
        );
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
    
    public void initialize (IProfile profile, TBSLogWrapper log) {
        try {
            final var client = TBSClient.getInstance ().getConnection (profile, log);
            
            TBSEmitterManager.restore ();
            final var emitters = TBSEmitterManager.getInstance ();
            
            try {
                log.info ("Loading bonds from portfolio (with data from Tinkoff and MOEX)...");
                portfolio = client.getUserService ().getAccountsSync ().parallelStream ()
                    . flatMap (acc -> {
                        try {                            
                            return client.getOperationsService ().getPortfolioSync (acc.getId ()).getPositions ().stream ();
                        } catch (Exception e) {
                            log.error ("Failed to load portfolio bonds", e);
                            return Stream.of ();
                        }
                    })
                    . filter (pos -> "bond".equalsIgnoreCase (pos.getInstrumentType ()))
                    . map (ppos -> {
                        final var bond = Optional.ofNullable (client.getInstrumentsService ().getBondByFigiSync (ppos.getFigi ()));
                        final var ticker = bond.map (ru.tinkoff.piapi.contract.v1.Bond::getTicker).orElse (null);
                        final var currency = bond.map (ru.tinkoff.piapi.contract.v1.Bond::getCurrency)
                            . map (Currency::from).orElse (null);
                        
                        return new Bond (profile, ticker, currency, ppos);
                    }).collect (Collectors.toList ());
            } catch (Exception e) {
                log.error ("Failed to load portfolio bonds due to some unexpected error", e);
                portfolio = new ArrayList <> ();
                scanned = new ArrayList <> ();
                return;
            }
            
            try {                
                log.info ("Loading data about bonds from Tinkoff and MOEX...");
                scanned = client.getInstrumentsService ().getAllBondsSync ().stream ()
                    . filter (instrument -> 
                        instrument.getApiTradeAvailableFlag ()
                        && instrument.getBuyAvailableFlag ()
                        && profile.getCurrencies ().contains (Currency.from (instrument.getCurrency ()))
                    ).parallel ()
                    . map (instr -> new Bond (profile, instr, false)).peek (bond -> emitters.addEmitter (bond.getEmitterId (), bond.getCode ()))
                    . filter (profile::testBond)//.limit (profile.getMaxResults ())
                    . collect (Collectors.toList ());
            } catch (Exception e) {
                log.error ("Failed to scan bonds due to some unexpected error", e);
                scanned = new ArrayList <> ();
                return;
            }
            
            emitters.dump ();
            updateMapping ();
        } catch (IOException ioe) {
            log.error ("Failed to scan bonds", ioe);
            portfolio = new ArrayList <> (); 
            scanned = new ArrayList <> ();
        }
    }
    
    public void analize (IProfile profile, TBSLogWrapper log) {
        portfolio.forEach (bond -> {
            bond.updateScore (profile);
        });
        
        scanned.forEach (bond -> {
            final var sameBond = getBondByTicker (bond.getCode (), false);
            bond.setLots (TBSUtils.mapIfNN (sameBond, Bond::getLots, 0L));
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
