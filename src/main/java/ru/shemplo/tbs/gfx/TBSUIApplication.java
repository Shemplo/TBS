package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.io.IOException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSClient;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.ITBSProfile;

@Slf4j
public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TBSBondsTable scannedBondsTable, portfolioBondsTable;
    @SuppressWarnings ("unused")
    private TBSBalanceControl balanceControl;
    private TBSPlannerTool plannerTool;
    private Text profileDetails;
    
    @Getter
    private Stage stage;
    
    @Getter
    private ITBSProfile profile;
    
    @Override
    public void start (Stage stage) throws Exception {
        Thread.currentThread ().setName ("TBS-Window-Thread");
        this.stage = stage;
        
        final var root = new VBox ();
        final var scene = new Scene (root);
        
        stage.setOnCloseRequest (we -> {
            try {
                TBSClient.getInstance ().close ();
            } catch (IOException ioe) {
                log.error ("Failed to close connection to Tinkoff", ioe);
            }
            
            try {
                TBSBackgroundExecutor.getInstance ().close ();
            } catch (Exception e) {
                log.error ("Failed to stop background executors", e);
            }
        });
        
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.setTitle ("Tinkoff Bonds Scanner | v1.0");
        stage.setMaximized (true);
        stage.setScene (scene);
        stage.show ();
        
        root.getChildren ().add (profileDetails = new Text ());
        profileDetails.setFont (Font.font ("Consolas", 10.0));
        VBox.setMargin (profileDetails, new Insets (12.0));
        
        final var tabs = new TabPane ();
        tabs.getStylesheets ().add (STYLE_TABS);
        VBox.setVgrow (tabs, Priority.ALWAYS);
        root.getChildren ().add (tabs);
        
        final var tabScanned = new Tab ("Scanned bonds");
        tabScanned.setContent (scannedBondsTable = new TBSBondsTable (TBSTableType.SCANNED));
        tabScanned.setClosable (false);
        tabs.getTabs ().add (tabScanned);
        
        final var tabPortfolio = new Tab ("Portfolio bonds");
        tabPortfolio.setContent (portfolioBondsTable = new TBSBondsTable (TBSTableType.PORTFOLIO));
        tabPortfolio.setClosable (false);
        tabs.getTabs ().add (tabPortfolio);
        
        final var tabPlanner = new Tab ("Planning tool");
        tabPlanner.setContent (plannerTool = new TBSPlannerTool ());
        tabPlanner.setClosable (false);
        tabs.getTabs ().add (tabPlanner);
        
        final var tabBalance = new Tab ("Balance control");
        tabBalance.setContent (balanceControl = new TBSBalanceControl ());
        tabBalance.setClosable (false);
        tabs.getTabs ().add (tabBalance);
        
        instance = this;
    }
    
    public void applyData (ITBSProfile profile) {
        this.profile = profile;
        
        profileDetails.setText (profile.getProfileDescription ());
        
        final var bondManager = TBSBondManager.getInstance ();
        scannedBondsTable.applyData (FXCollections.observableArrayList (
            bondManager.getScanned ().stream ().limit (profile.getMaxResults ()).map (Bond::getProxy).toList ()
        ));
        portfolioBondsTable.applyData (FXCollections.observableArrayList (
            bondManager.getPortfolio ().stream ().map (Bond::getProxy).toList ()
        ));
        
        plannerTool.applyData (profile);
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
