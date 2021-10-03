package ru.shemplo.tbs;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Getter
@RequiredArgsConstructor
public enum TBSProfile implements ITBSProfile {
    
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
        /*banned emitters      */ Set.of (-1L)
    ),
    
    RISCKY_RUB ("token.txt", true, 
        /*max results          */ 10, 
        /*inflation            */ 0.065,
        /*min months till end  */ 16L, 
        /*min coupons per year */ null, 
        /*max days till coupon */ 60L, 
        /*min nominal          */ 1000.0,
        /*min percentage       */ 10.0,
        /*max price            */ 1100.0,
        /*currencies           */ Set.of (Currency.RUB), 
        /*coupon values modes  */ Set.of (CouponValueMode.FIXED, CouponValueMode.NOT_FIXED),
        /*banned emitters      */ Set.of (-1L)
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
    
}
