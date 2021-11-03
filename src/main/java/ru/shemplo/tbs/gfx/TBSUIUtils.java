package ru.shemplo.tbs.gfx;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import ru.shemplo.tbs.TBSUtils;

public class TBSUIUtils {
    
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
    
}
