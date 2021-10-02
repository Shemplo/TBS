package ru.shemplo.tbs;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.ToString;
import ru.shemplo.tbs.moex.MOEXRequests;
import ru.shemplo.tbs.moex.MOEXResposeReader;
import ru.shemplo.tbs.moex.xml.Data;
import ru.shemplo.tbs.moex.xml.Row;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

@Getter
@ToString
public class Bond implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name, code;
    private Currency currency;
    
    private LocalDate start, end, nextCoupon;
    private long couponsPerYear;
    
    private long emitterId;
    
    private double nominalValue, percentage, lastPrice;
    private List <Coupon> coupons = new ArrayList <> ();
    
    private String primaryBoard;
    
    public Bond (MarketInstrument instrument) {
        currency = instrument.getCurrency ();
        
        final var MOEX_DESCRIPION_URL = MOEXRequests.makeBondDescriptionURLForMOEX (instrument.getTicker ());
        final var MOEX_COUPONS_URL = MOEXRequests.makeBondCouponsURLForMOEX (instrument.getTicker ());
        
        if (MOEX_DESCRIPION_URL != null) {
            final var MOEXData = MOEXResposeReader.read (MOEX_DESCRIPION_URL);
            MOEXData.getDescription ().ifPresent (description -> {
                name = description.getBondName ().orElse ("");
                code = description.getBondCode ().orElse ("");
                
                couponsPerYear = description.getBondCouponsPerYear ().orElse (1);
                nextCoupon = description.getBondNextCouponDate ().orElse (null);
                nominalValue = description.getBondNominalValue ().orElse (0.0);
                percentage = description.getBondPercentage ().orElse (0.0);
                emitterId = description.getBondEmitterID ().orElse (-1L);
                start = description.getBondStartDate ().orElse (null);
                end = description.getBondEndDate ().orElse (null);
            });
            
            MOEXData.getBoards ().ifPresent (boards -> {
                primaryBoard = boards.findPrimaryRow ().map (Row::getBoardid).orElse ("-");
            });
        }
        
        if (MOEX_COUPONS_URL != null) {
            final var MOEXCoupons = MOEXResposeReader.read (MOEX_COUPONS_URL);
            MOEXCoupons.getCoupons ().map (Data::getRows).ifPresent (cops -> {
                double previous = 0.0;
                for (final var coupon : Optional.ofNullable (cops.getRows ()).orElse (List.of ())) {
                    final var c = new Coupon (coupon, previous);
                    previous = c.getAmount ();
                    coupons.add (c);
                }
            });
        }
        
        final var MOEX_PRICE_URL = MOEXRequests.makeBondLastPriceURLForMOEX (primaryBoard, instrument.getTicker ());
        
        if (MOEX_PRICE_URL != null) {
            final var MOEXPrice = MOEXResposeReader.read (MOEX_PRICE_URL);
            MOEXPrice.getMarketData ().map (Data::getRows).ifPresent (price -> {
                final var lastValue = price.getRows () == null || price.getRows ().isEmpty () ? ""
                                    : price.getRows ().get (0).getLastPrice ();
                lastPrice = lastValue.isBlank () ? Double.MAX_VALUE : (Double.parseDouble (lastValue) * 10.0);
            });
        }
    }
    
    public CouponValueMode getCouponValuesMode () {
        final var amounts = coupons.stream ().map (Coupon::getAmount).collect (Collectors.toSet ());
        return amounts.isEmpty () ? CouponValueMode.UNDEFINED 
             : amounts.size () == 1 ? CouponValueMode.FIXED 
             : CouponValueMode.NOT_FIXED;
    }
    
    public long getYearsTillEnd () {
        return end == null ? 0L : LocalDate.now ().until (end, ChronoUnit.YEARS);
    }
    
    public long getMonthTillEnd () {
        return end == null ? 0L : LocalDate.now ().until (end, ChronoUnit.MONTHS);
    }
    
    public long getDaysTillNextCoupon () {
        return nextCoupon == null ? Long.MAX_VALUE : LocalDate.now ().until (nextCoupon, ChronoUnit.DAYS);
    }
    
    //private static final Predicate <Coupon> CONCIDER_COUPON = coupon -> LocalDate.now ().isBefore (coupon.getDate ());
    
    private double couponsCredit, nominalValueWithInflation, pureCredit;
    private boolean reliableCoupons;
    private double score = 0.0;
    
    public void updateScore (TBSProfile profile) {
        final var now = LocalDate.now ();
        
        final var months = now.until (end, ChronoUnit.MONTHS);
        final var days = now.until (end, ChronoUnit.DAYS);
        
        reliableCoupons = coupons.stream ().map (Coupon::isReliable).reduce (Boolean::logicalAnd).orElse (true);
        couponsCredit = coupons.stream ().mapToDouble (c -> c.getCredit (profile, now, end)).sum ();
        
        final var inflationFactor =  Math.pow (1 + profile.getInflation (), days / 365.0);
        final var lastPriceWithInflation = lastPrice / inflationFactor;
        nominalValueWithInflation = nominalValue / inflationFactor;
        
        pureCredit = nominalValueWithInflation + couponsCredit - lastPriceWithInflation;
        
        final var priceBalance = profile.getSafeMaxPrice (lastPrice) - lastPrice;
        final var monthsBalance = months - profile.getSafeMinMonths ();
        score = pureCredit + monthsBalance * 1.13 + priceBalance * 1.35;
    }
    
}
