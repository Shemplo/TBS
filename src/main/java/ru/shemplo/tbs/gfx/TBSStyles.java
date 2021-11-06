package ru.shemplo.tbs.gfx;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ru.shemplo.tbs.TBSConstants;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.CouponValueMode;
import ru.shemplo.tbs.gfx.table.TBSTableCell;

public class TBSStyles {
    
    public static final String STYLE_TABLES = "/table.css";
    public static final String STYLE_TABS = "/tabs.css";
    
    public static final Background BG_APPROPRIATE = new Background (new BackgroundFill (Color.HONEYDEW, null, null));
    public static final Background BG_SO_CLOSE = new Background (new BackgroundFill (Color.BEIGE, null, null));
    
    public static final Font FONT_DEFAULT = Font.font (null, FontWeight.NORMAL, 12.0);
    public static final Font FONT_BOLD = Font.font (null, FontWeight.BOLD, 12.0);
    
    public static final Color COLOR_POSITIVE = Color.GREEN;
    public static final Color COLOR_DEFAULT = Color.BLACK;
    public static final Color COLOR_NEGATIVE = Color.RED;
    public static final Color COLOR_NEUTRAL = Color.GRAY;
    
    public static <O> Consumer <TBSTableCell <O, SymbolOrImage>> linkIcon () {
        return cell -> {
            final var view = cell.getGraphic ();
            if (view == null) { return; }
            
            view.setCursor (Cursor.HAND);
            
            if (view instanceof Text text) {
                text.setFill (Color.BLUE);
            }
        };
    }
    
    public static <O, T extends Number> Consumer <TBSTableCell <O, T>> threshold (T threshold, double precision) {
        final var tvalue = threshold.doubleValue ();
        return cell -> {
            final var difference = TBSUtils.aOrB (cell.getItem ().doubleValue (), 0.0) - tvalue;
            if (difference > precision) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (difference < -precision) {
                cell.setTextFill (COLOR_NEGATIVE);
            } else {                    
                cell.setTextFill (COLOR_DEFAULT);
            }
        };
    }
    
    public static <O> Consumer <TBSTableCell <O, LocalDate>> sameMonth (LocalDate now) {
        return cell -> {
            final var value = TBSUtils.aOrB (cell.getItem (), TBSConstants.FAR_PAST);
            final var withinMonth = value.getMonthValue () == now.getMonthValue ();
            final var days = now.until (value, ChronoUnit.DAYS);
            
            if (withinMonth && days <= 14 && days >= 0) {
                cell.setTextFill (COLOR_POSITIVE);
                cell.setFont (FONT_BOLD);
            } else if (days <= 28 && days >= 0) {
                cell.setTextFill (COLOR_POSITIVE);
                cell.setFont (FONT_DEFAULT);
            } else if (days <= 42 && days >= 0) {
                cell.setTextFill (COLOR_DEFAULT); 
                cell.setFont (FONT_DEFAULT);
            } else {
                cell.setTextFill (COLOR_NEUTRAL);
                cell.setFont (FONT_DEFAULT);
            }
        };
    }
    
    public static <O> Consumer <TBSTableCell <O, CouponValueMode>> fixedCoupons () {
        return cell -> {
            final var value = cell.getItem ();
            if (value == CouponValueMode.FIXED) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (value == CouponValueMode.NOT_FIXED) {
                cell.setTextFill (COLOR_DEFAULT);
            } else if (value == CouponValueMode.UNDEFINED) {
                cell.setTextFill (COLOR_NEUTRAL);
            }
        };
    }
    
}
