package ru.shemplo.tbs.gfx.launcher;

import java.io.IOException;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSClient;
import ru.shemplo.tbs.TBSLogWrapper;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;

public class TBSStatistics {
    
    private final Pane root;
    
    public TBSStatistics (Window window) {
        root = new VBox ();
        
        final var scene = new Scene (root);
        
        final var stage = new Stage ();
        root.getChildren ().add (makeLoadingLayout ());
        
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Launcher | Statistics"));
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.initModality (Modality.WINDOW_MODAL);
        stage.setResizable (false);
        stage.initOwner (window);
        stage.setScene (scene);
        stage.setHeight (400);
        stage.setWidth (800);
        stage.show ();
    }
    
    private Text commentT;
    
    private Parent makeLoadingLayout () {
        final var column = new VBox (8.0);
        VBox.setVgrow (column, Priority.ALWAYS);
        column.setAlignment (Pos.CENTER);
        
        final var progressLine = new HBox ();
        progressLine.setAlignment (Pos.CENTER);
        progressLine.setFillHeight (false);
        column.getChildren ().add (progressLine);
        
        final var progressPB = new ProgressBar ();
        progressPB.setMinWidth (200);
        progressLine.getChildren ().add (progressPB);
        
        final var commentLine = new HBox ();
        commentLine.setAlignment (Pos.CENTER);
        commentLine.setFillHeight (false);
        column.getChildren ().add (commentLine);
        
        commentT = new Text ();
        commentLine.getChildren ().add (commentT);
        
        return column;
    }
    
    @SuppressWarnings ("unused")
    private Parent makeLayout () {
        return null;
    }
    
    public void calculateStatistics (IProfile profile) {
        TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
            try {
                Platform.runLater (() -> commentT.setText ("Loading operations from Tinkoff..."));
                
                final var logger = new TBSLogWrapper ();
                TBSClient.getInstance ().getConnection (profile, logger);
            } catch (IOException ioe) {
                
            }
        });
    }
    
}
