package ru.shemplo.tbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.gfx.TBSUIApplication;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.SandboxRegisterRequest;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

@Slf4j
public class RunTinkoffBondScanner {
    
    private static final File DUMP_FILE = new File ("dump.bin");
    
    public static void main (String ... args) {
        Locale.setDefault (Locale.ENGLISH);
        
        final var profile = TBSProfiler.fetchProfile (args);
        if (profile == null) {
            log.error ("Profile is not defined. Terminating scanner...");
            return;
        }
        
        @SuppressWarnings ("resource")
        final var scanner = new Scanner (System.in);
        
        if (DUMP_FILE.exists () && DUMP_FILE.canRead ()) {
            System.out.print (
                "There are dumped bonds (" + new Date (DUMP_FILE.lastModified ()) + "). "
                + "Do you want to restore them (`y` or any other value to deny)? "
            );
            
            final var decision = scanner.next ();
            if ("y".equals (decision)) {
                restoreBonds ();
            } else {
                initializeProfile (profile);
            }
        } else {            
            initializeProfile (profile);
        }
    }
    
    private static void initializeProfile (ITBSProfile profile) {
        try {
            log.info ("Reading token from file...");
            final var token = Files.readString (Paths.get (profile.getTokenFilename ()));
            openConnection (profile, token);
        } catch (IOException ioe) {
            log.error ("Failed to read token: " + ioe, ioe);
        }
    }
    
    private static void openConnection (ITBSProfile profile, String token) {
        log.info ("Connecting to Tinkoff API...");
        log.info ("Profile: {}", profile);
        try (final var client = new OkHttpOpenApi (token, !profile.isHighResponsible ())) {
            log.info ("Perform registration...");
            if (client.isSandboxMode ()) {
                client.getSandboxContext ().performRegistration (new SandboxRegisterRequest ()).join ();
            }
            
            searchForBonds (profile, client);
        } catch (Exception e) {
            log.error ("Unexpected exception in client: " + e, e);
        }
    }
    
    private static void searchForBonds (ITBSProfile profile, OpenApi client) {
        log.info ("Loading bonds from portfolio (with data from Tinkoff and MOEX)...");
        final var portfolio = searchForPortfolio (profile, client);
        //searchForFavorites (profile, client);
        
        log.info ("Loading data abount bonds from Tinkoff and MOEX...");
        final var bonds = client.getMarketContext ().getMarketBonds ().join ().getInstruments ().stream ()
            . filter (instrument -> profile.getCurrencies ().contains (instrument.getCurrency ())).parallel ()
            . map (Bond::new).filter (profile::testBond).limit (profile.getMaxResults ())
            . collect (Collectors.toList ());
        analizeBonds (profile, bonds, portfolio);
    }
    
    private static List <Bond> searchForPortfolio (ITBSProfile profile, OpenApi client) {
        return client.getUserContext ().getAccounts ().join ().getAccounts ().parallelStream ().flatMap (acc -> {
            return client.getPortfolioContext ().getPortfolio (acc.getBrokerAccountId ()).join ().getPositions ().stream ();
        }).filter (pos -> pos.getInstrumentType () == InstrumentType.BOND).map (Bond::new).toList ();
    }
    
    @SuppressWarnings ("unused") // Not supported by Tinkoff Open API yet
    private static void searchForFavorites (ITBSProfile profile, OpenApi client) {
        
    }
    
    private static void analizeBonds (ITBSProfile profile, List <Bond> bonds, List <Bond> portfolio) {
        log.info ("Analizing loaded bonds (total: {})...", bonds.size ());
        final var ticker2bonds = portfolio.stream ().collect (Collectors.toMap (
            Bond::getCode, Function.identity (), TBSUtils::aOrB
        ));
        
        portfolio.forEach (bond -> {
            bond.updateScore (profile);
        });
        
        bonds.forEach (bond -> {
            bond.setLots (TBSUtils.mapIfNN (ticker2bonds.get (bond.getCode ()), Bond::getLots, 0));
            bond.updateScore (profile);
        });
        
        bonds.sort (Comparator.comparing (Bond::getScore).reversed ());
        dumpBonds (profile, bonds, portfolio);
    }
    
    private static void dumpBonds (ITBSProfile profile, List <Bond> bonds, List <Bond> portfolio) {
        log.info ("Dumping bonds to a binary file...");
        try (
            final var fos = new FileOutputStream (DUMP_FILE);
            final var oos = new ObjectOutputStream (fos);
        ) {
            oos.writeObject (new Dump (profile, bonds, portfolio));
        } catch (IOException ioe) {
            log.error ("Failed to dump bonds (" + ioe + ")", ioe);
        }
        
        showResults (profile, bonds, portfolio);
    }
    
    private static void restoreBonds () {
        log.info ("Restoring bonds from a binary file...");
        try (
            final var fis = new FileInputStream (DUMP_FILE);
            final var ois = new ObjectInputStream (fis);
        ) {
            final var dump = (Dump) ois.readObject ();
            showResults (dump.getProfile (), dump.getBonds (), dump.getPortfolio ());
        } catch (IOException | ClassNotFoundException ioe) {
            log.error ("Failed to dump bonds (" + ioe + ")", ioe);
        }
    }
    
    private static void showResults (ITBSProfile profile, List <Bond> bonds, List <Bond> portfolio) {
        log.info ("Starting UI...");
        new Thread (() -> {
            Application.launch (TBSUIApplication.class);
        }, "Primary-Window-Thread").start ();
        
        while (TBSUIApplication.getInstance () == null) {}
        
        log.info ("Show results in UI...");
        TBSUIApplication.getInstance ().applyData (profile, bonds, portfolio);
    }
    
}
