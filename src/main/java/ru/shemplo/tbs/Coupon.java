package ru.shemplo.tbs;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import lombok.Getter;
import lombok.ToString;
import ru.shemplo.tbs.moex.xml.Row;

@Getter
@ToString
public class Coupon implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    
    private double amount;
    private boolean reliable = true;
    
    public Coupon (Row row, double previous) {
        final var amount = row.getValueAsDouble ();
        if (amount == null) { reliable = false; }
        
        this.amount = Optional.ofNullable (amount).orElse (previous);
        date = row.getCouponLocalDate ();
    }
    
    public double getCredit (TBSProfile profile, LocalDate forDate, LocalDate end) {
        if (date == null || !forDate.plusDays (profile.getSafeMaxDaysToCoupon ()).isBefore (date)) {
            return 0.0;
        }
        
        final var days = date.until (end, ChronoUnit.DAYS);
        final var credit = amount / Math.pow (1 + profile.getInflation (), days / 365.0);
        return reliable ? credit : (credit * 0.9);
    }
    
}
