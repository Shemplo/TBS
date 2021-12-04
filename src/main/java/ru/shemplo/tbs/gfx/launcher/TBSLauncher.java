package ru.shemplo.tbs.gfx.launcher;

import static ru.shemplo.tbs.TBSBondManager.*;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSDumpService;
import ru.shemplo.tbs.TBSPlanner;
import ru.shemplo.tbs.TBSPlanner.DistributionCategory;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.Dump;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.entity.LauncherDump;
import ru.shemplo.tbs.entity.PlanningDump;
import ru.shemplo.tbs.entity.Profile;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIApplication;

@Slf4j
public class TBSLauncher extends Application {
    
    public static final File PROFILES_FILE = new File ("profiles.bin");
    
    private ObservableList <IProfile> profiles;
    
    private Button openScannedBondsButton;
    private Button startNewScanningButton;
    private Button createProfileButton;
    private Button cloneProfileButton;
    private Button deleteProfileButton;
    
    private ListView <IProfile> profilesList;
    
    private Stage stage;
    
    @Override
    public void start (Stage stage) throws Exception {
        Thread.currentThread ().setName ("TBS-Launcher-Thread");
        
        final var root = new VBox (8.0);
        root.setPadding (new Insets (8.0));
        final var scene = new Scene (root);
        
        root.getChildren ().add (makeOpenExistingSection ());
        root.getChildren ().add (makeScanNewSection ());
        
        
        stage.setTitle ("Tinkoff Bonds Scanner | Launcher");
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.setResizable (false);
        stage.setScene (scene);
        stage.sizeToScene ();
        stage.show ();
        
        this.stage = stage;
    }
    
    private Parent makeOpenExistingSection () {
        final var line = new HBox (8.0);
        line.setAlignment (Pos.BASELINE_LEFT);
        
        final var dumpDate = TBSBondManager.getDumpDate ();
        openScannedBondsButton = new Button ("Open scanned bonds");
        openScannedBondsButton.setDisable (dumpDate == null);
        openScannedBondsButton.setOnMouseClicked (me -> {
            if (me.getButton () == MouseButton.PRIMARY && me.getClickCount () == 1) {
                restoreBonds ();
            }
        });
        line.getChildren ().add (openScannedBondsButton); 
        
        final var bondsTimestampText = new Text (dumpDate == null 
            ? "There is no scanned bonds dump file" 
            : "There is scanned bonds dump file (" + dumpDate + ")"
        );
        line.getChildren ().add (bondsTimestampText);
        
        return line;
    }
    
    private Parent makeScanNewSection () {
        final var line = new HBox (8.0);
        
        final var buttonsColumn = new VBox (8.0);
        line.getChildren ().add (buttonsColumn);
        
        startNewScanningButton = new Button ("Scan bonds");
        startNewScanningButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        buttonsColumn.getChildren ().add (startNewScanningButton);
        
        createProfileButton = new Button ("Create new profile");
        createProfileButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        createProfileButton.setOnMouseClicked (me -> openProfileForm (me, null));
        buttonsColumn.getChildren ().add (createProfileButton);
        
        cloneProfileButton = new Button ("Clone profile");
        cloneProfileButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        buttonsColumn.getChildren ().add (cloneProfileButton);
        
        deleteProfileButton = new Button ("Delete profile");
        deleteProfileButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        buttonsColumn.getChildren ().add (deleteProfileButton);
        
        final var setupColumn = new VBox (8.0);
        line.getChildren ().add (setupColumn);
        
        final var restoredDump = TBSDumpService.getInstance ().restore (PROFILES_FILE.getName (), LauncherDump.class);
        
        final var profilesDump = TBSUtils.mapIfNN (restoredDump, LauncherDump::getProfiles, new ArrayList <IProfile> ());
        profiles = FXCollections.observableArrayList (profilesDump);
        
        profilesList = new ListView <IProfile> (profiles);
        profilesList.setCellFactory (__ -> {
            final var cell = new TBSProfileCell ();
            cell.setOnMouseClicked (me -> {
                if (cell.getItem () != null && me.getClickCount () == 2) {
                    openProfileForm (me, cell);
                }
            });
            return cell;
        });
        profilesList.setBorder (TBSStyles.BORDER_DEFAULT);
        profilesList.setBackground (Background.EMPTY);
        HBox.setHgrow (profilesList, Priority.NEVER);
        profilesList.setMaxHeight (300.0);
        profilesList.setMinWidth (600.0);
        setupColumn.getChildren ().addAll (profilesList);
        
        final var itemProperty = profilesList.getSelectionModel ().selectedItemProperty ();
        startNewScanningButton.disableProperty ().bind (itemProperty.isNull ());
        deleteProfileButton.disableProperty ().bind (itemProperty.isNull ());
        cloneProfileButton.disableProperty ().bind (itemProperty.isNull ());
        
        startNewScanningButton.setOnMouseClicked (me -> {
            if (me.getButton () == MouseButton.PRIMARY && me.getClickCount () == 1) {
                loadCurrencyQuotes (itemProperty.get ());
            }
        });
        cloneProfileButton.setOnMouseClicked (me -> {
            profiles.add (itemProperty.get ().copy ());
            dumpLauncher ();
        });
        deleteProfileButton.setOnMouseClicked (me -> {
            profiles.remove (itemProperty.get ());
            dumpLauncher ();
        });
        
        return line;
    }
    
    private void openProfileForm (MouseEvent me, TBSProfileCell cell) {
        if (me.getButton () == MouseButton.PRIMARY) {
            IProfile profile = new Profile ();
            boolean newProfile = true;
            if (cell != null) {
                profile = cell.getItem ();
                newProfile = false;
            }
            
            final var scene = ((Node) me.getSource ()).getScene ();
            final var editor = new TBSProfileForm (scene.getWindow (), profile, newProfile);
            
            editor.setOnSaveRequest ((prof, add) -> {
                if (add) { // New profile should be added to list
                    profiles.add (prof); 
                }
                
                profilesList.refresh ();
                dumpLauncher ();
            });
        }
    }
    
    private void dumpLauncher () {
        TBSDumpService.getInstance ().dump (
            new LauncherDump (new ArrayList <> (profiles)), 
            PROFILES_FILE.getName ()
        );
    }
    
    private void loadCurrencyQuotes (IProfile profile) {
        final var currencyManager = TBSCurrencyManager.getInstance ();
        currencyManager.initialize (profile);
        
        log.info ("Quotes: {}", currencyManager.getStringQuotes ());
        
        searchForBonds (profile);
    }
    
    private void searchForBonds (IProfile profile) {
        final var bondManager = TBSBondManager.getInstance ();
        bondManager.initialize (profile);
        
        analizeBonds (profile);
    }
    
    private void analizeBonds (IProfile profile) {
        final var bondManager = TBSBondManager.getInstance ();
        log.info ("Analizing loaded bonds (total: {} + {})...", 
            bondManager.getScanned ().size (), 
            bondManager.getPortfolio ().size ()
        );
        
        bondManager.analize (profile);
        dumpBonds (profile);
    }
    
    private void dumpBonds (IProfile profile) {
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
    
    private void restoreBonds () {
        log.info ("Restoring bonds from a binary file...");
        final var dump = TBSDumpService.getInstance ().restore (
            TBSBondManager.DUMP_FILE.getName (), Dump.class
        );
        
        if (dump != null) {
            restorePlanningBonds ();
            showResults (dump.getProfile ());
        } else {
            log.error ("Dump was not restored. Exit...");
        }
    }
    
    private void restorePlanningBonds () {
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
    
    private void showResults (IProfile profile) {
        log.info ("Starting UI...");
        try {
            final var stage = new Stage (StageStyle.DECORATED);
            new TBSUIApplication ().start (stage);
            
            TBSUIApplication.getInstance ().applyData (profile);
            this.stage.close ();
            stage.show ();
        } catch (Exception e) {
            log.error (String.valueOf (e), e);
        }
        
        log.info ("Show results in UI...");
    }
    
}
