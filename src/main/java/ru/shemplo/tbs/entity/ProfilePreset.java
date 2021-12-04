package ru.shemplo.tbs.entity;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Getter
@RequiredArgsConstructor
public enum ProfilePreset implements IProfile {
    
    DEFAULT_RUB ("token.txt", true, 
        /*max results          */ 30, 
        /*inflation            */ 0.065,
        /*min months till end  */ new Range <> (24, null), 
        /*min coupons per year */ null, 
        /*max days till coupon */ new Range <> (null, 30), 
        /*min nominal (in RUB) */ new Range <> (1000.0, null),
        /*min percentage       */ new Range <> (6.0, null),
        /*max price (in RUB)   */ new Range <> (null, 1000.0),
        /*currencies           */ Set.of (Currency.RUB), 
        /*coupon values modes  */ Set.of (CouponValueMode.FIXED, CouponValueMode.NOT_FIXED),
        /*banned emitters      */ Set.of (-1L)
    ),
    
    RISCKY_RUB ("token.txt", true, 
        /*max results          */ 10, 
        /*inflation            */ 0.065,
        /*min months till end  */ new Range <> (16, null), 
        /*min coupons per year */ null, 
        /*max days till coupon */ new Range <> (null, 60), 
        /*min nominal (in RUB) */ new Range <> (1000.0, null),
        /*min percentage       */ new Range <> (10.0, null),
        /*max price (in RUB)   */ new Range <> (null, 1100.0),
        /*currencies           */ Set.of (Currency.RUB), 
        /*coupon values modes  */ Set.of (CouponValueMode.FIXED, CouponValueMode.NOT_FIXED),
        /*banned emitters      */ Set.of (-1L)
    )
    
    ;
    
    private final String token;
    private final boolean highResponsible;
    private final long maxResults;
    private final double inflation;
    
    private final Range <Integer> monthsTillEnd, couponsPerYear, daysToCoupon;
    private final Range <Double> nominalValue, percentage, price;
    
    private final Set <Currency> currencies;
    private final Set <CouponValueMode> couponValuesModes;
    private final Set <Long> bannedEmitters;
    
    @Override
    public boolean isEditable () {
        return false;
    }
    
    @Override
    public IProfile copy () {
        return new Profile (name (), token, highResponsible, maxResults, inflation, 
            monthsTillEnd.copy (), couponsPerYear.copy (), daysToCoupon.copy (), 
            nominalValue.copy (), percentage.copy (), price.copy (), 
            Set.copyOf (couponValuesModes), Set.copyOf (currencies),
            Set.copyOf (bannedEmitters)
        );
    }
    
}
