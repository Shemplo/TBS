package ru.shemplo.tbs.gfx.launcher;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.MappingROProperty;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.TinkoffRequests;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.IEmitter;
import ru.shemplo.tbs.entity.LinkedObject;
import ru.shemplo.tbs.entity.LinkedSymbolOrImage;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIUtils;
import ru.shemplo.tbs.gfx.table.TBSEditTableCell;
import ru.shemplo.tbs.gfx.table.TBSTableCell;
import ru.shemplo.tbs.moex.MOEXRequests;

public class TBSEmittersTable {
    
    private TableView <IEmitter> table;
    
    public TBSEmittersTable (Window window) {
        final var root = new VBox ();
        
        final var scene = new Scene (root);
        
        final var stage = new Stage ();
        root.getChildren ().add (makeEmittersTable ());
        
        TBSEmitterManager.restore ();
        table.setItems (TBSEmitterManager.getInstance ().getEmitters ());
        
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Launcher | Emitters editor"));
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.initModality (Modality.WINDOW_MODAL);
        stage.setResizable (false);
        stage.initOwner (window);
        stage.setScene (scene);
        stage.setHeight (800);
        stage.setWidth (800);
        //stage.sizeToScene ();
        stage.show ();
    }
    
    private Parent makeEmittersTable () {
        table = new TableView <> ();
        table.getStylesheets ().setAll (STYLE_TABLES);
        table.setBackground (TBSStyles.BG_TABLE);
        VBox.setVgrow (table, Priority.ALWAYS);
        table.setSelectionModel (null);
        table.setBorder (Border.EMPTY);
        
        final var linkIcon = TBSStyles.<IEmitter> linkIcon ();
        
        table.getColumns ().add (TBSUIUtils.<IEmitter, Long> buildTBSTableColumn ()
            .name ("Id").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (90.0).sortable (false)
            .propertyFetcher (emitter -> emitter.getRWProperty ("id", () -> -1L))
            .converter (null).highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IEmitter, String, TextField> buildTBSEditTableColumn ()
            .name ("Name").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (250.0).sortable (false)
            .propertyFetcher (emitter -> new MappingROProperty <> (
                emitter.<String> getRWProperty ("name", () -> null), 
                v -> new LinkedObject <> (String.valueOf (emitter.getId ()), v)
            ))
            .fieldSupplier (this::makeCustomLotsValueField)
            .converter ((c, v) -> v.getObject ())
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IEmitter, BondCreditRating> buildTBSSelectTableColumn ()
            .name ("Credit rating").tooltip (null)
            .alignment (Pos.BASELINE_RIGHT).minWidth (125.0).sortable (false)
            .propertyFetcher (emitter -> new MappingROProperty <> (
                emitter.<BondCreditRating> getRWProperty ("rating", () -> null), 
                v -> new LinkedObject <> (String.valueOf (emitter.getId ()), v)
            ))
            .onSelection (this::handleEmitterRatingChange)
            .enumeration (BondCreditRating.class)
            .highlighter (null)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IEmitter, LinkedSymbolOrImage> buildTBSIconTableColumn ()
            .name ("").tooltip (null).minWidth (30.0).sortable (false)
            .propertyFetcher (b -> makeSyncProperty (b, "🔄")).highlighter (linkIcon)
            .onClick (this::handleExploreBrowserColumnClick)
            .build ());
        table.getColumns ().add (TBSUIUtils.<IEmitter, Date> buildTBSTableColumn ()
            .name ("Updated").tooltip (null)
            .alignment (Pos.BASELINE_LEFT).minWidth (200.0).sortable (false)
            .propertyFetcher (emitter -> emitter.getRWProperty ("updated", () -> null))
            .converter ((c, v) -> v.toString ()).highlighter (null)
            .build ());
        
        return table;
    }
    
    private TextField makeCustomLotsValueField (TBSEditTableCell <IEmitter, String, TextField> cell) {
        final var field = new TextField ();
        field.setPadding (new Insets (1, 4, 1, 4));
        field.textProperty ().addListener ((__, ___, value) -> {
            TBSUtils.doIfNN (cell.getItem (), item -> {
                final var emitters = TBSEmitterManager.getInstance ();
                TBSUtils.doIfNN (emitters.getEmitterById (Long.parseLong (item.getLink ())), b -> {
                    b.getRWProperty ("name", () -> null).set (value);
                    emitters.dump ();
                });
            });
        });
        
        return field;
    }
    
    private void handleEmitterRatingChange (LinkedObject <BondCreditRating> item, BondCreditRating selected) {
        final var selectedF = TBSUtils.aOrB (selected, BondCreditRating.UNDEFINED);
        
        TBSUtils.doIfNN (item, i -> {
            final var id = Long.parseLong (item.getLink ());
            
            TBSEmitterManager.getInstance ().getEmitterById (id).getRWProperty (
                "rating", () -> BondCreditRating.UNDEFINED
            ).set (selectedF);
        });
    }
    
    private ObjectProperty <LinkedSymbolOrImage> makeSyncProperty (IEmitter bond, String symbol) {
        final var idPropery = bond.getRWProperty ("id", () -> -1L);
        final var property = new SimpleObjectProperty <LinkedSymbolOrImage> ();
        property.bind (Bindings.createObjectBinding (
            () -> LinkedSymbolOrImage.symbol (symbol, String.valueOf (idPropery.get ())), 
            idPropery
        ));
        return property;
    }
    
    private void handleExploreBrowserColumnClick (MouseEvent me, TBSTableCell <IEmitter, LinkedSymbolOrImage> cell) {
        if (TBSUIUtils.SIMPLE_CLICK.test (me)) {
            TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
                final var emitterId = Long.parseLong (cell.getItem ().getLink ());
                
                final var tickers = TBSBondManager.getInstance ().getScanned ().stream ()
                    . filter (bond -> Objects.equals (bond.getEmitterId (), emitterId))
                    . map (Bond::getCode).toList ();
                
                if (tickers.isEmpty ()) {
                    Platform.runLater (() -> {                        
                        final var alert = new Alert (AlertType.WARNING);
                        alert.setContentText ("No scanned bonds for this emitter");
                        alert.setTitle ("Failed to fetch emitter parameters");
                        alert.setHeaderText (alert.getTitle ());
                        alert.setResizable (false);
                        
                        alert.show ();
                    });
                }
                
                for (final var ticker : tickers) {
                    try {
                        fetchDataFromTinkoff (emitterId, ticker);
                        fetchDataFromMOEX (emitterId, ticker);
                    } catch (IOException ioe) {
                        ioe.printStackTrace ();
                        continue;
                    }
                }
            });
        }
    }
    
    private final Pattern EMITTER_PAGE_REGEXP = Pattern.compile ("<a.+href='/(.*)'>Отчетность эмитента</a>");
    
    private final Pattern EMITTER_NAME_REGEXP = Pattern.compile ("<h1>Отчетность эмитента (.*)</h1>");
    private final Pattern EMITTER_NAME_2_REGEXP = Pattern.compile ("<h1.*>(.+),.+\\(.*\\)</h1>");
    
    
    private void fetchDataFromMOEX (long emitterId, String ticker) throws IOException {
        final var responseBond = MOEXRequests.loadBondPageContent (ticker);
        final var matcherBond = EMITTER_PAGE_REGEXP.matcher (responseBond);
        
        if (matcherBond.find ()) {
            final var responseEmitter = MOEXRequests.loadEmitterPageContent (matcherBond.group (1));
            final var matcherName = EMITTER_NAME_REGEXP.matcher (responseEmitter);
            
            if (matcherName.find ()) {
                applyName (emitterId, matcherName.group (1));
            }
        } else {
            final var matcherName = EMITTER_NAME_2_REGEXP.matcher (responseBond);
            if (matcherName.find ()) {
                final var rn = matcherName.group (1); // raw name
                final var name = Character.toUpperCase (rn.charAt (0)) + rn.substring (1);
                applyName (emitterId, name);
            }
        }
    }
    
    // <div class="SecurityHeaderPure__panelText_xF3LC" data-qa-file="SecurityHeaderPure">Умеренный</div>
    private final Pattern EMITTER_CR_RATING_REGEXP = Pattern.compile (
        "(?:<div(?:\\w|\\s|-|=|\"|/|\\.)*>)(?!<div)Рейтинг эмитента</div><div(?:\\w|\\s|-|=|\"|/|\\.)*>(\\w+)</div>",
        Pattern.UNICODE_CHARACTER_CLASS
    );
    
    private void fetchDataFromTinkoff (long emitterId, String ticker) throws IOException {
        final var responseBond = TinkoffRequests.loadBondPageContent (ticker);
        final var matcherRating = EMITTER_CR_RATING_REGEXP.matcher (responseBond);
        
        if (matcherRating.find ()) {
            TBSUtils.fetchCreditRating (matcherRating.group (1)).ifPresent (rating -> {
                TBSEmitterManager.getInstance ().getEmitterById (emitterId).getRWProperty ("rating", () -> null).set (rating);
            });
        } else {
            System.out.println ("Rating information is not found"); // SYSOUT
        }
    }
    
    
    private void applyName (long emitterId, String name) {
        TBSEmitterManager.getInstance ().getEmitterById (emitterId).getRWProperty ("name", () -> null).set (name);
    }
    
}
