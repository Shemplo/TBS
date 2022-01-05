package ru.shemplo.tbs.gfx.launcher;

import static ru.shemplo.tbs.gfx.TBSStyles.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.shemplo.tbs.MappingROProperty;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.IEmitter;
import ru.shemplo.tbs.entity.LinkedObject;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.TBSStyles;
import ru.shemplo.tbs.gfx.TBSUIUtils;
import ru.shemplo.tbs.gfx.table.TBSEditTableCell;

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
        stage.setWidth (600);
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
    
}
