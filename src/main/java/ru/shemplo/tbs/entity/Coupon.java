package ru.shemplo.tbs.entity;

import static ru.shemplo.tbs.TBSConstants.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.NavigableSet;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.shemplo.tbs.moex.xml.Row;

@Getter
@ToString
@AllArgsConstructor
public class Coupon extends AbstractObservableEntity <ICoupon> implements ICoupon {
    
    private static final long serialVersionUID = 1L;

    public static final String NEXT_COUPON = "➥";
    
    private LocalDate date, record;
    
    private double amount;
    private boolean reliable = true;
    private String symbol = "";
    
    public Coupon (Row row, Coupon previous, NavigableSet <LocalDate> offers, LocalDate now) {
        final var amount = row.getValueAsDouble ();
        if (amount == null) { reliable = false; }
        
        this.amount = Optional.ofNullable (amount).orElse (previous.getAmount ());
        record = row.getCouponRecordLocalDate ();
        date = row.getCouponLocalDate ();
        
        if (!NOW.isAfter (date) && NOW.isAfter (previous.getDate ())) {
            symbol = NEXT_COUPON;
        }
        
        final var offer = offers.floor (date);
        if (offer != null && !offer.isAfter (date) && offer.isAfter (previous.getDate ())) {
            symbol += "\u2B7F";
        }
    }
    
    public double getCredit (IProfile profile, LocalDate forDate, LocalDate end) {
        /*
        if (date == null || !forDate.plusDays (profile.getSafeMaxDaysToCoupon ()).isBefore (date)) {
            return 0.0;
        }
        */
        
        final var days = date.until (end, ChronoUnit.DAYS);
        final var credit = amount / Math.pow (1 + profile.getInflation (), days / 365.0);
        return reliable ? credit : (credit * 0.9);
    }
    
    public boolean isNextCoupon () {
        return symbol.contains (NEXT_COUPON);
    }
    
}
