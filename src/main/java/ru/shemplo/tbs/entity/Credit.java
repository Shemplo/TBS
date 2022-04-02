package ru.shemplo.tbs.entity;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSUtils;

@Getter
@RequiredArgsConstructor (access =  AccessLevel.PRIVATE)
public class Credit extends AbstractObservableEntity <ICredit> implements ICredit {
    
    private static final long serialVersionUID = 1L;

    private final Bond bond;
    
    private final Long lots;
    
    private final Coupon coupon;
    
    @Override
    public String toString () {
        return String.format ("Credit[bond=%s, lots=%d, value=%f]", bond.getCode (), lots, getCreditValue ());
    }
    
    public boolean isPlanned () {
        return lots != null;
    }
    
    public double getCreditValue () {
        final var currencyCoeff = TBSCurrencyManager.getInstance ().getToRubCoefficient (bond.getCurrency ());
        final var credit = TBSUtils.mapIfNN (coupon, Coupon::getAmount, bond.getNominalValue ());
        return credit * currencyCoeff * TBSUtils.aOrB (lots, bond.getLots ());
    }
    
    public LocalDate getCreditDate () {
        return TBSUtils.mapIfNN (coupon, Coupon::getDate, bond.getEnd ());
    }
    
    // 
    
    public static Credit coupon (Bond bond, Long lots, Coupon coupon) {
        return new Credit (bond, lots, coupon);
    }
    
    public static Credit portfolioCoupon (Bond bond, Coupon coupon) {
        return new Credit (bond, null, coupon);
    }
    
    public static Credit bondEnd (Bond bond, Long lots) {
        return new Credit (bond, lots, null);
    }
    
    public static Credit portfolioBondEnd (Bond bond) {
        return new Credit (bond, null, null);
    }
    
}
