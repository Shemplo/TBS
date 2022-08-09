package ru.shemplo.tbs.gfx.launcher;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
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
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSBondDetailsManager;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSClient;
import ru.shemplo.tbs.TBSDumpService;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.TBSPlanner;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.entity.LauncherDump;
import ru.shemplo.tbs.entity.Profile;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIApplication;
import ru.shemplo.tbs.gfx.TBSUIUtils;

@Slf4j
public class TBSLauncher extends Application {
    
    private static final File PROFILES_FILE = new File ("launcher.bin");
    
    private ObservableList <IProfile> profiles;
    
    private Button openScannedBondsButton;
    private Button openEmittertEditorButton;
    private Button startNewScanningButton;
    private Button createProfileButton;
    private Button deleteProfileButton;
    private Button cloneProfileButton;
    private Button openStatisticsButton;
    private Button openCouponsCalendarButton;
    private Text bondsDumpDateText;
    
    private boolean dontStopExecutors = false;
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
        root.getChildren ().add (makeEmittersEditorSection ());
        //root.getChildren ().add (makeStatisticsSection ());
        
        stage.setOnCloseRequest (we -> {
            if (!dontStopExecutors) {
                try {
                    TBSBackgroundExecutor.getInstance ().close ();
                    TBSClient.getInstance ().close ();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        stage.setTitle ("Tinkoff Bonds Scanner | Launcher");
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.setResizable (false);
        stage.setScene (scene);
        stage.sizeToScene ();
        stage.show ();
        
        this.stage = stage;
        
        openScannedBondsButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doOpenBonds ()));
        openScannedBondsButton.setOnAction (me -> doOpenBonds ());
        
        openEmittertEditorButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doOpenEmitters (scene)));
        openEmittertEditorButton.setOnAction (ae -> doOpenEmitters (scene));
        
        final var profileProperty = profilesList.getSelectionModel ().selectedItemProperty ();
        openStatisticsButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doOpenStatistics (scene, profileProperty)));
        openStatisticsButton.setOnAction (ae -> doOpenStatistics (scene, profileProperty));
        
        openCouponsCalendarButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doOpenPendingCoupons (scene, profileProperty)));
        openCouponsCalendarButton.setOnAction (ae -> doOpenPendingCoupons (scene, profileProperty));
        
        startNewScanningButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doStartNewScanning (scene, profileProperty)));
        startNewScanningButton.setOnAction (ae -> doStartNewScanning (scene, profileProperty));
        
        cloneProfileButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doClone (profileProperty)));
        cloneProfileButton.setOnAction (ae -> doClone (profileProperty));
        
        deleteProfileButton.setOnMouseClicked (me -> TBSUIUtils.doIfSimpleClick (me, () -> doDelete (profileProperty)));
        deleteProfileButton.setOnAction (ae -> doDelete (profileProperty));
        
        scene.setOnKeyTyped (ke -> {
            final var ch = ke.getCharacter ().charAt (0);
            if (Character.isDigit (ch)) {
                final var index = (Character.getNumericValue (ch) + 9) % 10;
                profilesList.getSelectionModel ().select (index);
                startNewScanningButton.requestFocus ();
            }
        });
    }
    
    private void doOpenBonds () {
        final var profile = TBSBondManager.restore ();
        TBSBondDetailsManager.restore ();
        TBSEmitterManager.restore ();
        TBSPlanner.restore ();
        
        openTBSApplication (profile);
    }
    
    private void doOpenEmitters (Scene scene) {
        TBSBondManager.restore ();
        
        new TBSEmittersTable (scene.getWindow ());
    }
    
    private void doOpenStatistics (Scene scene, ReadOnlyProperty <IProfile> profileProperty) {
        new TBSStatistics (scene.getWindow ()).loadStatistics (profileProperty.getValue ());
    }
    
    private void doOpenPendingCoupons (Scene scene, ReadOnlyProperty <IProfile> profileProperty) {
        new TBSCouponsCalendar (scene.getWindow ()).loadPendingCoupons (profileProperty.getValue ());
    }
    
    private void doStartNewScanning (Scene scene, ReadOnlyObjectProperty <IProfile> profileProperty) {
        new TBSScanLog (scene.getWindow (), this).scan (profileProperty.get (), this::updateOpenExistingSection);
    }
    
    private void doClone (ReadOnlyObjectProperty <IProfile> profileProperty) {
        profiles.add (profileProperty.get ().copy ());
        dumpLauncher ();
    }
    
    private void doDelete (ReadOnlyObjectProperty <IProfile> profileProperty) {
        profiles.remove (profileProperty.get ());
        dumpLauncher ();
    }
    
    private Parent makeOpenExistingSection () {
        final var line = new HBox (8.0);
        line.setAlignment (Pos.BASELINE_LEFT);
        
        openScannedBondsButton = new Button ("Open scanned _bonds");
        openScannedBondsButton.setMnemonicParsing (true);
        openScannedBondsButton.setMinWidth (150.0);
        line.getChildren ().add (openScannedBondsButton); 
        
        line.getChildren ().add (bondsDumpDateText = new Text ());
        
        updateOpenExistingSection ();
        return line;
    }
    
    private Parent makeEmittersEditorSection () {
        final var line = new HBox (8.0);
        line.setAlignment (Pos.BASELINE_LEFT);
        
        openEmittertEditorButton = new Button ("Open _emitters editor");
        openEmittertEditorButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        openEmittertEditorButton.setMnemonicParsing (true);
        line.getChildren ().add (openEmittertEditorButton);
        
        line.getChildren ().add (new Text ("Edit name or credit rating of emitters which bonds were scanned at least once"));
        
        return line;
    }
    
    private Parent makeScanNewSection () {
        final var line = new HBox (8.0);
        
        final var buttonsColumn = new VBox (8.0);
        line.getChildren ().add (buttonsColumn);
        
        startNewScanningButton = new Button ("Scan bonds");
        startNewScanningButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        buttonsColumn.getChildren ().add (startNewScanningButton);
        
        createProfileButton = new Button ("Create _new profile");
        createProfileButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        createProfileButton.setOnMouseClicked (me -> openProfileForm (me, null));
        createProfileButton.setMnemonicParsing (true);
        buttonsColumn.getChildren ().add (createProfileButton);
        
        cloneProfileButton = new Button ("C_lone profile");
        cloneProfileButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        cloneProfileButton.setMnemonicParsing (true);
        buttonsColumn.getChildren ().add (cloneProfileButton);
        
        deleteProfileButton = new Button ("_Delete profile");
        deleteProfileButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        deleteProfileButton.setMnemonicParsing (true);
        buttonsColumn.getChildren ().add (deleteProfileButton);
        
        openCouponsCalendarButton = new Button ("Open coupons _calendar");
        openCouponsCalendarButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        openCouponsCalendarButton.setMnemonicParsing (true);
        VBox.setMargin (openCouponsCalendarButton, new Insets (8.0, 0.0, 0.0, 0.0));
        buttonsColumn.getChildren ().add (openCouponsCalendarButton);
        
        openStatisticsButton = new Button ("Calculate _statistics");
        openStatisticsButton.minWidthProperty ().bind (openScannedBondsButton.widthProperty ());
        openStatisticsButton.setMnemonicParsing (true);
        buttonsColumn.getChildren ().add (openStatisticsButton);
        
        final var setupColumn = new VBox (8.0);
        line.getChildren ().add (setupColumn);
        
        final var restoredDump = TBSDumpService.getInstance ().<LauncherDump> restore (PROFILES_FILE.getName ());
        
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
        profilesList.setMinWidth (650.0);
        setupColumn.getChildren ().addAll (profilesList);
        
        final var itemProperty = profilesList.getSelectionModel ().selectedItemProperty ();
        openCouponsCalendarButton.disableProperty ().bind (itemProperty.isNull ());
        startNewScanningButton.disableProperty ().bind (itemProperty.isNull ());
        openStatisticsButton.disableProperty ().bind (itemProperty.isNull ());
        deleteProfileButton.disableProperty ().bind (itemProperty.isNull ());
        cloneProfileButton.disableProperty ().bind (itemProperty.isNull ());
        
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
    
    private void updateOpenExistingSection () {
        final var dumpDate = TBSBondManager.getDumpDate ();
        openScannedBondsButton.setDisable (dumpDate == null);
        bondsDumpDateText.setText (dumpDate == null 
            ? "There is no scanned bonds dump file" 
            : "There is scanned bonds dump file (" + dumpDate + ")"
        );
    }
    
    public void openTBSApplication (IProfile profile) {
        log.info ("Starting UI...");
        try {
            final var stage = new Stage (StageStyle.DECORATED);
            new TBSUIApplication ().start (stage);
            
            TBSUIApplication.getInstance ().applyData (profile);
            dontStopExecutors = true;
            this.stage.close ();
            stage.show ();
        } catch (Exception e) {
            log.error (String.valueOf (e), e);
        }
        
        log.info ("Show results in UI...");
    }
    
}
