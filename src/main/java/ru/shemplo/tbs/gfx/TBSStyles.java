package ru.shemplo.tbs.gfx;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import ru.shemplo.tbs.TBSConstants;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.CouponValueMode;
import ru.shemplo.tbs.entity.LinkedSymbolOrImage;
import ru.shemplo.tbs.gfx.table.TBSTableCell;

public class TBSStyles {
    
    public static final String STYLE_MULTI_CHART = "/multi-chart.css";
    public static final String STYLE_TABLES = "/table.css";
    public static final String STYLE_TABS = "/tabs.css";
    
    //public static final Background BG_NO_OUTLINES = new Background (new BackgroundFill (Color.HONEYDEW, null, null));
    public static final Background BG_TILE_ACCENT = new Background (new BackgroundFill (
        Color.rgb (200, 200, 200, 0.5), new CornerRadii (4.0), Insets.EMPTY)
    );
    public static final Background BG_TABLE = new Background (new BackgroundFill (Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background BG_APPROPRIATE = new Background (new BackgroundFill (Color.HONEYDEW, null, null));
    public static final Background BG_SO_CLOSE = new Background (new BackgroundFill (Color.BEIGE, null, null));
    
    public static final Border BORDER_DEFAULT = new Border (new BorderStroke (Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, null));
    
    public static final Font FONT_MONO_12 = Font.font ("monospace", FontWeight.NORMAL, 12.0);
    public static final Font FONT_NORMAL_16 = Font.font (null, FontWeight.NORMAL, 16.0);
    public static final Font FONT_NORMAL_20 = Font.font (null, FontWeight.NORMAL, 20.0);
    public static final Font FONT_DEFAULT = Font.font (null, FontWeight.NORMAL, 12.0);
    public static final Font FONT_BOLD_12 = Font.font (null, FontWeight.BOLD, 12.0);
    public static final Font FONT_BOLD_14 = Font.font (null, FontWeight.BOLD, 14.0);
    public static final Font FONT_BOLD_20 = Font.font (null, FontWeight.BOLD, 20.0);
    
    public static final Color COLOR_POSITIVE = Color.GREEN;
    public static final Color COLOR_DEFAULT = Color.BLACK;
    public static final Color COLOR_NEGATIVE = Color.RED;
    public static final Color COLOR_NEUTRAL = Color.GRAY;
    
    public static <F> Consumer <TBSTableCell <F, LinkedSymbolOrImage>> linkIcon () {
        return cell -> {
            final var view = cell.getGraphic ();
            if (view == null) { return; }
            
            view.setCursor (Cursor.HAND);
            
            if (view instanceof Text text) {
                text.setFill (Color.BLUE);
            }
        };
    }
    
    public static <F, T extends Number> Consumer <TBSTableCell <F, T>> threshold (T threshold, double precision) {
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
    
    public static <F, T extends Number> Consumer <TBSTableCell <F, T>> thresholdNotBefore (
        T threshold, double precision, LocalDate after,
        Function <TBSTableCell <F, T>, LocalDate> dateFetcher
    ) {
        final var tvalue = threshold.doubleValue ();
        return cell -> {
            final var difference = TBSUtils.aOrB (cell.getItem ().doubleValue (), 0.0) - tvalue;
            final var date = TBSUtils.aOrB (dateFetcher.apply (cell), TBSConstants.FAR_PAST);
            if (date.isBefore (after)) {
                cell.setTextFill (COLOR_NEUTRAL);
            } else if (difference > precision) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (difference < -precision) {
                cell.setTextFill (COLOR_NEGATIVE);
            } else {                    
                cell.setTextFill (COLOR_DEFAULT);
            }
        };
    }
    
    public static <F> Consumer <TBSTableCell <F, LocalDate>> sameMonth (LocalDate now) {
        return sameMonth (now, cell -> cell.getItem ());
    }
    
    public static <F, V> Consumer <TBSTableCell <F, V>> sameMonth (
        LocalDate now, 
        Function <TBSTableCell <F, V>, LocalDate> dateFetcher
    ) {
        return cell -> {
            final var value = TBSUtils.aOrB (dateFetcher.apply (cell), TBSConstants.FAR_PAST);
            final var withinMonth = value.getMonthValue () == now.getMonthValue ();
            final var days = now.until (value, ChronoUnit.DAYS);
            
            if (withinMonth && days <= 14 && days >= 0) {
                cell.setTextFill (COLOR_POSITIVE);
                cell.setFont (FONT_BOLD_12);
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
    
    public static <F> Consumer <TBSTableCell <F, CouponValueMode>> fixedCoupons () {
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
    
    public static <F> Consumer <TBSTableCell <F, BondCreditRating>> creditRating () {
        return cell -> {
            final var value = cell.getItem ();
            if (value == BondCreditRating.HIGH) {
                cell.setTextFill (COLOR_POSITIVE);
                cell.setFont (FONT_BOLD_12);
            } else if (value == BondCreditRating.MEDIUM) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (value == BondCreditRating.LOW) {
                cell.setTextFill (COLOR_NEGATIVE);
            } else if (value == BondCreditRating.SPECULATIVE) {
                cell.setTextFill (COLOR_NEGATIVE);
                cell.setFont (FONT_BOLD_12);
            } else if (value == BondCreditRating.UNDEFINED) {
                cell.setTextFill (COLOR_NEUTRAL);
            }
        };
    }
    
}
