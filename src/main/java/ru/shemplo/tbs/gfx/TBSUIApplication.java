package ru.shemplo.tbs.gfx;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSBondDetailsManager;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSClient;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.gfx.component.TileWithHeader;

@Slf4j
public class TBSUIApplication extends Application {

    @Getter
    private static volatile TBSUIApplication instance;
    
    private TBSBondsTable scannedBondsTable, portfolioBondsTable;
    @SuppressWarnings ("unused")
    private TBSBalanceControl balanceControl;
    private TBSPlannerTool plannerTool;
    
    private Text profileDetails;
    private TabPane tabs;
    
    @Getter
    private Stage stage;
    
    @Getter
    private IProfile profile;
    
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
        stage.setTitle ("Tinkoff Bonds Scanner");
        stage.setMinHeight (600.0);
        stage.setMinWidth (800.0);
        stage.setMaximized (true);
        stage.setScene (scene);
        //stage.show ();
        
        final var topBarRow = new HBox (8.0);
        root.getChildren ().add (topBarRow);
        
        profileDetails = new Text ();
        profileDetails.setFont (Font.font ("Consolas", 12.0));
        
        final var profileScroller = new ScrollPane (profileDetails);
        topBarRow.getChildren ().add (profileScroller);
        profileScroller.setPadding (new Insets (16.0, 4.0, 0.0, 12.0));
        profileScroller.setHbarPolicy (ScrollBarPolicy.AS_NEEDED);
        profileScroller.setVbarPolicy (ScrollBarPolicy.NEVER);
        profileScroller.setMinViewportHeight (8.0);
        profileScroller.setBackground (null);
        
        final var bondTickerField = new TextField ();
        bondTickerField.setFont (Font.font ("Consolas", 12.0));
        bondTickerField.setTooltip (new Tooltip ("Type ticker and press <Enter> to open bond details"));
        bondTickerField.setMinWidth (200.0);
        
        final var bondTickerFieldTitle = new TileWithHeader <> ("Open bond details by ticker", bondTickerField);
        HBox.setMargin (bondTickerFieldTitle, new Insets (8.0, 8.0, 4.0, 0.0));
        topBarRow.getChildren ().add (bondTickerFieldTitle);
        
        tabs = new TabPane ();
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
        
        bondTickerField.setOnAction (ae -> {
            final var ticker = bondTickerField.getText ();
            if (TBSUtils.notBlank (ticker)) {
                openBondTab (bondTickerField.getText ());                
                bondTickerField.setText ("");
            }
        });
        
        instance = this;
    }
    
    private void openBondTab (String ticker) {
        final var clearTicker = ticker.trim ();
        
        final var existing = tabs.getTabs ().stream ()
            . filter (t -> t.getText ().contains (clearTicker))
            . findFirst ();
        
        if (existing.isPresent ()) {            
            tabs.getSelectionModel ().select (existing.get ());            
        } else {
            final var tab = new Tab (clearTicker);
            tab.setOnClosed (e -> TBSBondDetailsManager.getInstance ().removeDetailed (profile, clearTicker));
            tab.setContent (new TBSBondDetails (tab, profile, clearTicker));
            tabs.getTabs ().add (tab);
            
            tabs.getSelectionModel ().select (tab);
        }
    }
    
    public void applyData (IProfile profile) {
        this.profile = profile;
        
        profileDetails.setText (profile.getProfileDescription ());
        
        final var bondDetailsManager = TBSBondDetailsManager.getInstance ();
        final var bondManager = TBSBondManager.getInstance ();
        
        scannedBondsTable.applyData (FXCollections.observableArrayList (
            bondManager.getScanned ().stream ().limit (profile.getMaxResults ()).map (Bond::getProxy).toList ()
        ));
        portfolioBondsTable.applyData (FXCollections.observableArrayList (
            bondManager.getPortfolio ().stream ().map (Bond::getProxy).toList ()
        ));
        
        plannerTool.applyData (profile);
        
        for (final var detailed : bondDetailsManager.getDetailed ()) {
            final var tab = new Tab (detailed.getCode ());
            tab.setOnClosed (e -> bondDetailsManager.removeDetailed (profile, detailed.getCode ()));
            tab.setContent (new TBSBondDetails (tab, detailed, profile));
            tabs.getTabs ().add (tab);
        }
    }
    
    public void openLinkInBrowser (URL url) {
        openLinkInBrowser (url.toExternalForm ());
    }
    
    public void openLinkInBrowser (String url) {
        getHostServices ().showDocument (url);
    }
    
}
