package ru.shemplo.tbs.gfx.launcher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSBondDetailsManager;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSDumpService;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.TBSLogWrapper;
import ru.shemplo.tbs.TBSPlanner;
import ru.shemplo.tbs.entity.BondsDump;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIUtils;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;

public class TBSScanLog {
    
    private final TBSLauncher launcher;
    private final Stage stage;
        
    private volatile SimpleBooleanProperty isScanning, isError;
    private final TBSLogWrapper logger;
    private volatile Button openBonds;
    private volatile TextArea logArea;
    private volatile Text message;
    
    public TBSScanLog (Window window, TBSLauncher launcher) {
        this.launcher = launcher;
        
        final var root = new VBox ();
        root.setPadding (new Insets (8.0));
        
        final var scene = new Scene (root);
        
        isScanning = new SimpleBooleanProperty ();
        isError = new SimpleBooleanProperty ();
        logger = new TBSLogWrapper ();
                
        stage = new Stage ();
        root.getChildren ().add (makeView ());
        
        stage.setOnCloseRequest (we -> {
            if (isScanning.get ()) { we.consume (); }
        });
        
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Launcher | Scan log"));
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.initModality (Modality.WINDOW_MODAL);
        stage.setResizable (false);
        stage.initOwner (window);
        stage.setScene (scene);
        stage.sizeToScene ();
        stage.show ();
    }
    
    private Parent makeView () {
        final var column = new VBox (8.0);
        
        logArea = new TextArea ();
        logArea.setBorder (TBSStyles.BORDER_DEFAULT);
        logArea.setBackground (Background.EMPTY);
        logArea.setMinHeight (200.0);
        logArea.setMinWidth (600.0);
        logArea.setEditable (false);
        logArea.textProperty ().bind (Bindings.createStringBinding (
            () -> logger.getLines ().stream ().collect (Collectors.joining (System.lineSeparator ())), 
            logger.getLines ()
        ));
        column.getChildren ().add (logArea);
        
        final var progress = new ProgressBar ();
        progress.progressProperty ().bind (Bindings.createDoubleBinding (() -> isScanning.get () ? -1.0 : 0.0, isScanning));
        progress.disableProperty ().bind (isScanning.not ());
        progress.setMinWidth (logArea.getMinWidth ());
        column.getChildren ().add (progress);
        
        final var bottomLine = new HBox (8.0);
        bottomLine.setAlignment (Pos.CENTER_LEFT);
        column.getChildren ().add (bottomLine);
        
        openBonds = new Button ("Open bonds");
        openBonds.disableProperty ().bind (Bindings.or (isScanning, isError));
        bottomLine.getChildren ().add (openBonds);
        
        bottomLine.getChildren ().add (message = new Text (
            "Please, do not close this window until bonds are scanned"
        ));
        
        return column;
    }
    
    private final File LOG_FILE = new File ("scan.log");
    
    public void scan (IProfile profile, Runnable onScanFinished) {
        isScanning.set (true);
        isError.set (false);
        
        TBSEmitterManager.restore ();
        TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
            try {
                logger.getLines ().clear ();
                
                final var currencyManager = TBSCurrencyManager.getInstance ();
                currencyManager.initialize (profile, logger);
                
                logger.info ("Quotes: {}", currencyManager.getStringQuotes ());
                
                final var bondManager = TBSBondManager.getInstance ();
                bondManager.initialize (profile, logger);
                
                logger.info ("Analizing loaded bonds (total: {} + {})...", 
                    bondManager.getScanned ().size (), 
                    bondManager.getPortfolio ().size ()
                );
                
                bondManager.analize (profile, logger);
                
                logger.info ("Dumping bonds to a binary file...");
                
                TBSDumpService.getInstance ().dump (
                    new BondsDump (profile, currencyManager, bondManager), 
                    TBSBondManager.DUMP_FILE.getName ()
                );
                
                Files.writeString (LOG_FILE.toPath (), logArea.getText (), StandardOpenOption.TRUNCATE_EXISTING);
                onScanFinished.run ();
            } catch (ApiRuntimeException apire) {
                logger.error ("Failed to scan bonds [Tinkoff API error]", apire);
                isError.set (true);
            } catch (Exception e) {
                logger.error ("Failed to scan bonds [Unexpected error]", e);
                isError.set (true);
            } finally {
                Platform.runLater (() -> {
                    if (!isError.get ()) {
                        openBonds.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doOpenBonds (profile)));                        
                        openBonds.setOnAction (ae -> doOpenBonds (profile));
                    }
                    
                    message.setText ("Log is saved to `scan.log` file, you can close this window");
                    isScanning.set (false);
                    
                    openBonds.requestFocus ();
                });
            }
        });
    }
    
    private void doOpenBonds (IProfile profile) {
        stage.close (); 
        
        TBSBondDetailsManager.restore ();
        TBSPlanner.restore ();
        launcher.openTBSApplication (profile);
    }
    
}
