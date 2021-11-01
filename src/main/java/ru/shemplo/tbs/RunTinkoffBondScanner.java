package ru.shemplo.tbs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.Dump;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.shemplo.tbs.gfx.TBSUIApplication;
import ru.tinkoff.invest.openapi.OpenApi;
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
            log.info ("Perform registration in Tinkoff API...");
            if (client.isSandboxMode ()) {
                client.getSandboxContext ().performRegistration (new SandboxRegisterRequest ()).join ();
            }
            
            loadCurrencyQuotes (profile, client);
        } catch (Exception e) {
            log.error ("Unexpected exception in client: " + e, e);
        }
    }
    
    private static void loadCurrencyQuotes (ITBSProfile profile, OpenApi client) {
        final var currencyManager = TBSCurrencyManager.getInstance ();
        currencyManager.initialize (profile, client, log);
        
        log.info ("Quotes: {}", currencyManager.getStringQuotes ());
        
        searchForBonds (profile, client);
    }
    
    private static void searchForBonds (ITBSProfile profile, OpenApi client) {
        final var bondManager = TBSBondManager.getInstance ();
        bondManager.initialize (profile, client, log);
        
        analizeBonds (profile);
    }
    
    private static void analizeBonds (ITBSProfile profile) {
        final var bondManager = TBSBondManager.getInstance ();
        log.info ("Analizing loaded bonds (total: {} + {})...", 
            bondManager.getScanned ().size (), 
            bondManager.getPortfolio ().size ()
        );
        
        bondManager.analize (profile);
        dumpBonds (profile);
    }
    
    private static void dumpBonds (ITBSProfile profile) {
        log.info ("Dumping bonds to a binary file...");
        final var currencyManager = TBSCurrencyManager.getInstance ();
        final var bondManager = TBSBondManager.getInstance ();
        
        try (
            final var fos = new FileOutputStream (DUMP_FILE);
            final var oos = new ObjectOutputStream (fos);
        ) { 
            oos.writeObject (new Dump (profile, currencyManager, bondManager));
        } catch (IOException ioe) {
            log.error ("Failed to dump bonds (" + ioe + ")", ioe);
        }
        
        showResults (profile);
    }
    
    private static void restoreBonds () {
        log.info ("Restoring bonds from a binary file...");
        try (
            final var fis = new FileInputStream (DUMP_FILE);
            final var ois = new ObjectInputStream (fis);
        ) {
            final var dump = (Dump) ois.readObject ();
            dump.getBondManager ().updateMapping ();
            
            showResults (dump.getProfile ());
        } catch (IOException | ClassNotFoundException ioe) {
            log.error ("Failed to dump bonds (" + ioe + ")", ioe);
        }
    }
    
    private static void showResults (ITBSProfile profile) {
        log.info ("Starting UI...");
        new Thread (() -> {
            Application.launch (TBSUIApplication.class);
        }, "Primary-Window-Thread").start ();
        
        while (TBSUIApplication.getInstance () == null) {}
        
        log.info ("Show results in UI...");
        TBSUIApplication.getInstance ().applyData (profile);
    }
    
}
