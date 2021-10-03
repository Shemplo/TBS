package ru.shemplo.tbs;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Getter
@ToString
@RequiredArgsConstructor (onConstructor = @__(@Builder))
public class TBSCustomProfile implements ITBSProfile {
    
    private static final long serialVersionUID = 1L;

    private final String name;
    
    private final String tokenFilename;
    private final boolean highResponsible;
    private final long maxResults;
    private final double inflation;
    
    private final Long monthsTillEnd, couponsPerYear, maxDaysToCoupon;
    private final Double nominalValue, minPercentage, maxPrice;
    
    private final Set <Currency> currencies;
    private final Set <CouponValueMode> couponValuesModes;
    private final Set <Long> bannedEmitters;
    
    @Override
    public String name () {
        return getName ();
    }
    
}