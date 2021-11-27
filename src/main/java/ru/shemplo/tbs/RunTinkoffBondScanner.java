package ru.shemplo.tbs;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.TBSPlanner.DistributionCategory;
import ru.shemplo.tbs.entity.Dump;
import ru.shemplo.tbs.entity.ITBSProfile;
import ru.shemplo.tbs.entity.PlanningDump;
import ru.shemplo.tbs.gfx.TBSUIApplication;

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
                + "Do you want to restore them (`y` to confirm, `q` to exit or any other value to deny)? "
            );
            
            final var decision = scanner.next ();
            if ("y".equals (decision)) {
                restoreBonds ();
            } else if ("q".equals (decision)) {
                return;
            } else {
                loadCurrencyQuotes (profile);
            }
        } else {            
            loadCurrencyQuotes (profile);
        }
    }
    
    private static void loadCurrencyQuotes (ITBSProfile profile) {
        final var currencyManager = TBSCurrencyManager.getInstance ();
        currencyManager.initialize (profile);
        
        log.info ("Quotes: {}", currencyManager.getStringQuotes ());
        
        searchForBonds (profile);
    }
    
    private static void searchForBonds (ITBSProfile profile) {
        final var bondManager = TBSBondManager.getInstance ();
        bondManager.initialize (profile);
        
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
        
        TBSDumpService.getInstance ().dump (
            new Dump (profile, currencyManager, bondManager), 
            DUMP_FILE.getName ()
        );
        
        restorePlanningBonds ();
        showResults (profile);
    }
    
    private static void restoreBonds () {
        log.info ("Restoring bonds from a binary file...");
        final var dump = TBSDumpService.getInstance ().restore (
            DUMP_FILE.getName (), Dump.class
        );
        
        if (dump != null) {
            restorePlanningBonds ();
            showResults (dump.getProfile ());
        } else {
            log.error ("Dump was not restored. Exit...");
        }
    }
    
    private static void restorePlanningBonds () {
        log.info ("Restoring planning bonds from a binary file...");
        if (TBSPlanner.DUMP_FILE.exists ()) {
            TBSDumpService.getInstance ().restore (
                TBSPlanner.DUMP_FILE.getName (), 
                PlanningDump.class
            );
        } else {
            TBSPlanner.getInstance ().updateParameters (
                DistributionCategory.SUM, 0.0, 0.0
            );
        }
    }
    
    private static void showResults (ITBSProfile profile) {
        log.info ("Starting UI...");
        new Thread (() -> Application.launch (TBSUIApplication.class)).start ();
        
        while (TBSUIApplication.getInstance () == null) {}
        
        log.info ("Show results in UI...");
        TBSUIApplication.getInstance ().applyData (profile);
    }
    
}
