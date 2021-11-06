package ru.shemplo.tbs.gfx;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import lombok.Builder;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.gfx.table.TBSIconTableCell;
import ru.shemplo.tbs.gfx.table.TBSTableCell;
import ru.shemplo.tbs.gfx.table.TBSToggleTableCell;

public class TBSUIUtils {
    
    /*
    public static <F, S> TableColumn <F, F> makeTBSTableColumn (
        String name, String tooltip, BiFunction <TableRow <F>, F, S> converter, boolean sortable, 
        double minWidth, Pos alignment, BiConsumer <TBSTableCell <F, S>, S> highlighter
    ) {
        final var column = new TableColumn <F, F> (name);
        column.setCellFactory (__ -> {
            final var cell = new TBSTableCell <> (converter, highlighter, alignment);
            TBSUtils.doIfNN (tooltip, t -> cell.setTooltip (new Tooltip (t)));
            return cell;
        });
        column.setCellValueFactory (cell -> {
            return new SimpleObjectProperty <> (cell.getValue ());
        });
        column.setPrefWidth (minWidth);
        column.setSortable (sortable);
        column.setMinWidth (minWidth);
        
        return column;
    }
    
    public static <F, S> TableColumn <F, F> makeTBSTableColumn (
        String name, String tooltip, Function <F, S> converter, boolean sortable, 
        double minWidth, Pos alignment, BiConsumer <TBSTableCell <F, S>, S> highlighter
    ) {
        return makeTBSTableColumn (name, tooltip, 
            (r, f) -> TBSUtils.mapIfNN (converter, c -> c.apply (f), null), 
            sortable, minWidth, alignment, highlighter
        );
    }
    */
    
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
    public static <F, S> TableColumn <F, SymbolOrImage> makeTBSIconTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, 
        Function <F, ObjectProperty <SymbolOrImage>> propertyFetcher, 
        BiConsumer <MouseEvent, TBSTableCell <F, SymbolOrImage>> onClick,
        Consumer <TBSTableCell <F, SymbolOrImage>> highlighter
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
    public static <F, S> TableColumn <F, LinkFlag> makeTBSToggleTableColumn (
        String name, String tooltip, boolean sortable, double minWidth, 
        Function <F, ObjectProperty <LinkFlag>> propertyFetcher, 
        Consumer <TBSTableCell <F, LinkFlag>> highlighter,
        BiConsumer <LinkFlag, Boolean> onToggle
    ) {
        final var column = makeTBSTableColumnBase (name, sortable, minWidth, propertyFetcher);
        column.setCellFactory (__ -> {
            final var cell = new TBSToggleTableCell <> (onToggle, highlighter);
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
