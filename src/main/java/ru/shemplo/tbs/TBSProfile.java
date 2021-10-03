package ru.shemplo.tbs;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Getter
@RequiredArgsConstructor
public enum TBSProfile {
    
    DEFAULT_RUB ("token.txt", true, 
        /*max results          */ 30, 
        /*inflation            */ 0.065,
        /*min months till end  */ 24L, 
        /*min coupons per year */ null, 
        /*max days till coupon */ 30L, 
        /*min nominal          */ 1000.0,
        /*min percentage       */ 6.0,
        /*max price            */ 1000.0,
        /*currencies           */ Set.of (Currency.RUB), 
        /*coupon values modes  */ Set.of (CouponValueMode.FIXED, CouponValueMode.NOT_FIXED),
        /*banned E             */ Set.of (-1L)
    )
    
    ;
    
    private final String tokenFilename;
    private final boolean highResponsible;
    private final long maxResults;
    private final double inflation;
    
    private final Long monthsTillEnd, couponsPerYear, maxDaysToCoupon;
    private final Double nominalValue, minPercentage, maxPrice;
    
    private final Set <Currency> currencies;
    private final Set <CouponValueMode> couponValuesModes;
    private final Set <Long> bannedEmitters;
    
    public boolean testBond (Bond bond) {
        return bond != null && currencies.contains (bond.getCurrency ())
            && couponValuesModes.contains (bond.getCouponValuesMode ())
            && !bannedEmitters.contains (bond.getEmitterId ())
            && (maxDaysToCoupon == null ? true : bond.getDaysToCoupon () <= maxDaysToCoupon)
            && (couponsPerYear == null ? true : bond.getCouponsPerYear () >= couponsPerYear)
            && (monthsTillEnd == null ? true : bond.getMonthToEnd () >= monthsTillEnd)
            && (minPercentage == null ? true : bond.getPercentage () >= minPercentage)
            && (maxPrice == null ? true : bond.getLastPrice () <= maxPrice)
            && (nominalValue == null ? true : bond.getNominalValue () >= nominalValue);
    }
    
    public long getSafeMinMonths () {
        return monthsTillEnd == null ? 0 : monthsTillEnd;
    }
    
    public double getSafeMaxPrice (double bondLastPrice) {
        return maxPrice == null ? bondLastPrice * 1.1 : maxPrice;
    }
    
    public long getSafeMaxDaysToCoupon () {
        return maxDaysToCoupon == null ? 0 : maxDaysToCoupon;
    }
    
    public String getProfileDescription () {
        return String.format (
            "Name: %s,  Max results: %d,  Inflation: %.1f%%,  Months: %d [↥],  C / Y: %d [↥],  Days to C: %d [↧],"
            + "  Nominal: %.1f [↥],  MOEX %%: %.1f [↥],  Price: %.1f [↧],  Currencies: %s,  C modes: %s", 
            name (), maxResults, inflation * 100, monthsTillEnd, couponsPerYear, maxDaysToCoupon, 
            nominalValue, minPercentage, maxPrice, currencies, couponValuesModes
        );
    }
    
}
