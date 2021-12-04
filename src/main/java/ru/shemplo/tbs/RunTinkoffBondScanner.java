package ru.shemplo.tbs;

import java.util.Locale;

import javafx.application.Application;
import ru.shemplo.tbs.gfx.launcher.TBSLauncher;

public class RunTinkoffBondScanner {
    
    public static void main (String ... args) {
        Locale.setDefault (Locale.ENGLISH);
        
        new Thread (() -> Application.launch (TBSLauncher.class)).start ();
    }
    
}
