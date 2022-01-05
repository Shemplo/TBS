package ru.shemplo.tbs.entity;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.tinkoff.invest.openapi.model.rest.Currency;

@Builder
@ToString
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile implements IProfile {
    
    private static final long serialVersionUID = 1L;

    private String name;
    
    @ToString.Exclude
    private String token;
    private boolean highResponsible;
    private long maxResults;
    private double inflation;
    
    @Default
    private Range <Integer> monthsTillEnd = new Range (null, null);
    
    @Default
    private Range <Integer> couponsPerYear = new Range (null, null);
    
    @Default
    private Range <Integer> daysToCoupon = new Range (null, null);
    
    @Default
    private Range <Double> nominalValue = new Range (null, null); 
    
    @Default
    private Range <Double> percentage = new Range (null, null);
    
    @Default
    private Range <Double> price = new Range (null, null);
    
    @Default
    private Set <CouponValueMode> couponValuesModes = new HashSet <> ();
    
    @Default
    private Set <Currency> currencies = new HashSet <> ();
    
    @Default
    private Set <BondCreditRating> creditRatings = new HashSet <> ();
    
    @Default
    private Set <Long> bannedEmitters = new HashSet <> ();
    
    @Override
    public String name () {
        return getName ();
    }
    
    @Override
    public boolean isEditable () {
        return true;
    }
    
    public Profile copy () {
        return new Profile (name, token, highResponsible, maxResults, inflation, 
            monthsTillEnd.copy (), couponsPerYear.copy (), daysToCoupon.copy (), 
            nominalValue.copy (), percentage.copy (), price.copy (), 
            Set.copyOf (couponValuesModes), Set.copyOf (currencies),
            Set.copyOf (creditRatings), Set.copyOf (bannedEmitters)
        );
    }
    
}
