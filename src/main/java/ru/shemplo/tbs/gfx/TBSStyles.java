package ru.shemplo.tbs.gfx;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ru.shemplo.tbs.CouponValueMode;

public class TBSStyles {
    
    public static final String STYLE_TABLES = "/table.css";
    public static final String STYLE_TABS = "/tabs.css";
    
    public static final Background BG_APPROPRIATE = new Background (new BackgroundFill (Color.HONEYDEW, null, null));
    public static final Background BG_SO_CLOSE = new Background (new BackgroundFill (Color.BEIGE, null, null));
    
    public static final Font FONT_BOLD = Font.font (null, FontWeight.BOLD, 12.0);
    
    public static final Color COLOR_POSITIVE = Color.GREEN;
    public static final Color COLOR_DEFAULT = Color.BLACK;
    public static final Color COLOR_NEGATIVE = Color.RED;
    public static final Color COLOR_NEUTRAL = Color.GRAY;
    
    public static <O, T extends Number> BiConsumer <TBSTableCell <O, T>, T> threshold (T threshold, double precision) {
        final var tvalue = threshold.doubleValue ();
        return (cell, value) -> {
            final var difference = value.doubleValue () - tvalue;
            if (difference > precision) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (difference < -precision) {
                cell.setTextFill (COLOR_NEGATIVE);
            } else {                    
                cell.setTextFill (COLOR_DEFAULT);
            }
        };
    }
    
    public static <O> BiConsumer <TBSTableCell <O, LocalDate>, LocalDate> sameMonth (LocalDate now) {
        return (cell, value) -> {
            final var withinMonth = now.until (value, ChronoUnit.DAYS) <= 28;
            if (withinMonth && value.getMonthValue () == now.getMonthValue ()) {
                cell.setTextFill (COLOR_POSITIVE);
                cell.setFont (FONT_BOLD);
            } else if (now.until (value, ChronoUnit.DAYS) <= 14) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (!withinMonth) {
                cell.setTextFill (COLOR_NEUTRAL);
            } else {
                cell.setTextFill (COLOR_DEFAULT);                
            }
        };
    }
    
    public static <O> BiConsumer <TBSTableCell <O, CouponValueMode>, CouponValueMode> fixedCoupons () {
        return (cell, value) -> {
            if (value == CouponValueMode.FIXED) {
                cell.setTextFill (COLOR_POSITIVE);
            } else if (value == CouponValueMode.UNDEFINED) {
                cell.setTextFill (COLOR_NEUTRAL);
            }
        };
    }
    
}