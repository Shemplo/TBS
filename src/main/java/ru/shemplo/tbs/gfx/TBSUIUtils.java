package ru.shemplo.tbs.gfx;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Builder;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.LinkedObject;
import ru.shemplo.tbs.entity.LinkedSymbolOrImage;
import ru.shemplo.tbs.gfx.table.TBSEditTableCell;
import ru.shemplo.tbs.gfx.table.TBSIconTableCell;
import ru.shemplo.tbs.gfx.table.TBSSelectTableCell;
import ru.shemplo.tbs.gfx.table.TBSTableCell;
import ru.shemplo.tbs.gfx.table.TBSToggleTableCell;

public class TBSUIUtils {
    
    public static Predicate <MouseEvent> SIMPLE_CLICK = me -> me.getButton () == MouseButton.PRIMARY && me.getClickCount () == 1;
    
    public static void doIfSimpleClick (MouseEvent me, Runnable task) {
        if (SIMPLE_CLICK.test (me)) {
            task.run ();
        }
    }
    
    @Builder (builderMethodName = "buildTBSTableColumn")
    public static <F, S> TableColumn <F, S> makeTBSTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, Pos alignment, 
        Function <F, ObjectProperty <S>> propertyFetcher, 
        BiFunction <TableRow <F>, S, String> converter, 
        Consumer <TBSTableCell <F, S>> highlighter
    ) {
        final var column = makeTBSTableColumnBase (name, sortable, minWidth, propertyFetcher);
        column.setCellFactory (__ -> {
            final var cell = new TBSTableCell <> (converter, highlighter, alignment);
            TBSUtils.doIfNN (tooltip, t -> cell.setTooltip (new Tooltip (t)));
            return cell;
        });
        
        return column;
    }
    
    @Builder (builderMethodName = "buildTBSIconTableColumn", builderClassName = "IconTableColumnBuilder")
    public static <F, S> TableColumn <F, LinkedSymbolOrImage> makeTBSIconTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, 
        Function <F, ObjectProperty <LinkedSymbolOrImage>> propertyFetcher, 
        BiConsumer <MouseEvent, TBSTableCell <F, LinkedSymbolOrImage>> onClick,
        Consumer <TBSTableCell <F, LinkedSymbolOrImage>> highlighter
    ) {
        final var column = makeTBSTableColumnBase (name, sortable, minWidth, propertyFetcher);
        column.setCellFactory (__ -> {
            final var cell = new TBSIconTableCell <> (highlighter, onClick);
            TBSUtils.doIfNN (tooltip, t -> cell.setTooltip (new Tooltip (t)));
            return cell;
        });
        
        return column;
    }
    
    @Builder (builderMethodName = "buildTBSToggleTableColumn", builderClassName = "ToggleTableColumnBuilder")
    public static <F, S> TableColumn <F, LinkedObject <Boolean>> makeTBSToggleTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, 
        Function <F, ObjectProperty <LinkedObject <Boolean>>> propertyFetcher, 
        Consumer <TBSTableCell <F, LinkedObject <Boolean>>> highlighter,
        BiConsumer <LinkedObject <Boolean>, Boolean> onToggle
    ) {
        final var column = makeTBSTableColumnBase (name, sortable, minWidth, propertyFetcher);
        column.setCellFactory (__ -> {
            final var cell = new TBSToggleTableCell <> (onToggle, highlighter);
            TBSUtils.doIfNN (tooltip, t -> cell.setTooltip (new Tooltip (t)));
            return cell;
        });
        
        return column;
    }
    
    @Builder (builderMethodName = "buildTBSEditTableColumn", builderClassName = "EditTableColumnBuilder")
    public static <F, S, TF extends TextField> TableColumn <F, LinkedObject <S>> makeTBSEditTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, Pos alignment, 
        Function <TBSEditTableCell <F, S, TF>, TF> fieldSupplier,
        Function <F, ObjectProperty <LinkedObject <S>>> propertyFetcher, 
        BiFunction <TableRow <F>, LinkedObject <S>, String> converter, 
        Consumer <TBSTableCell <F, LinkedObject <S>>> highlighter
    ) {
        final var column = makeTBSTableColumnBase (name, sortable, minWidth, propertyFetcher);
        column.setCellFactory (__ -> {
            final var cell = new TBSEditTableCell <> (fieldSupplier, converter, highlighter);
            TBSUtils.doIfNN (tooltip, t -> cell.setTooltip (new Tooltip (t)));
            return cell;
        });
        
        return column;
    }
    
    @Builder (builderMethodName = "buildTBSSelectTableColumn", builderClassName = "SelectTableColumnBuilder")
    public static <F, S extends Enum <S>> TableColumn <F, LinkedObject <S>> makeTBSSelectTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, Pos alignment, Class <S> enumeration,
        Function <F, ObjectProperty <LinkedObject <S>>> propertyFetcher, 
        Consumer <TBSTableCell <F, LinkedObject <S>>> highlighter,
        BiConsumer <LinkedObject <S>, S> onSelection
    ) {
        final var column = makeTBSTableColumnBase (name, sortable, minWidth, propertyFetcher);
        column.setCellFactory (__ -> {
            final var cell = new TBSSelectTableCell <> (enumeration, onSelection, highlighter, alignment);
            TBSUtils.doIfNN (tooltip, t -> cell.setTooltip (new Tooltip (t)));
            return cell;
        });
        
        return column;
    }
    
    public static <F, S> TableColumn <F, S> makeTBSTableColumnBase (
        String name, boolean sortable, double minWidth, 
        Function <F, ObjectProperty <S>> property
    ) {
        final var column = new TableColumn <F, S> (name);
        column.setCellValueFactory (cell -> {
            final var value = cell.getValue ();
            return property.apply (value);
        });
        
        column.setPrefWidth (minWidth);
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
}
