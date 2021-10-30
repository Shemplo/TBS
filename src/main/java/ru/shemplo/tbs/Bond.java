package ru.shemplo.tbs;

import static ru.shemplo.tbs.TBSConstants.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.shemplo.tbs.moex.MOEXRequests;
import ru.shemplo.tbs.moex.MOEXResposeReader;
import ru.shemplo.tbs.moex.xml.Data;
import ru.shemplo.tbs.moex.xml.Row;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.PortfolioPosition;

@Getter
@ToString
public class Bond implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name, code;
    private Currency currency;
    
    @Setter
    private int lots;
    
    private LocalDate start, end, nextCoupon;
    private long couponsPerYear;
    private LocalDate now;
    
    private long emitterId;
    
    private double nominalValue, percentage, lastPrice;
    
    private NavigableSet <LocalDate> offers = new TreeSet <> ();
    private List <Coupon> coupons = new ArrayList <> ();
    
    private String primaryBoard;
    
    public Bond (MarketInstrument instrument) {
        this (instrument.getTicker (), instrument.getCurrency (), NOW, 0);
    }
    
    public Bond (PortfolioPosition portfolio) {
        this (
            portfolio.getTicker (), portfolio.getAveragePositionPrice ().getCurrency (), 
            FAR_PAST, portfolio.getLots ()
        );
    }
    
    private Bond (String ticker, Currency currency, LocalDate scoreNow, int lots) {
        this.currency = currency;
        this.now = scoreNow;
        this.lots = lots;
        
        final var MOEX_DESCRIPION_URL = MOEXRequests.makeBondDescriptionURLForMOEX (ticker);
        final var MOEX_COUPONS_URL = MOEXRequests.makeBondCouponsURLForMOEX (ticker);
        
        if (MOEX_DESCRIPION_URL != null) {
            final var MOEXData = MOEXResposeReader.read (MOEX_DESCRIPION_URL);
            MOEXData.getDescription ().ifPresent (description -> {
                name = description.getBondName ().orElse ("");
                code = description.getBondCode ().orElse ("");
                
                couponsPerYear = description.getBondCouponsPerYear ().orElse (1);
                nextCoupon = description.getBondNextCouponDate ().orElse (null);
                nominalValue = description.getBondNominalValue ().orElse (1.0);
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
            MOEXCoupons.getOffers ().map (Data::getRows).ifPresent (offs -> {
                for (final var offer : Optional.ofNullable (offs.getRows ()).orElse (List.of ())) {
                    Optional.ofNullable (offer.getOfferLocalDate ()).ifPresent (offers::add);
                }
            });
            
            MOEXCoupons.getCoupons ().map (Data::getRows).ifPresent (cops -> {
                Coupon previous = new Coupon (FAR_PAST, 0.0, true, "");
                
                for (final var coupon : Optional.ofNullable (cops.getRows ()).orElse (List.of ())) {
                    coupons.add (previous = new Coupon (coupon, previous, offers, now));
                }
            });
        }
        
        final var MOEX_PRICE_URL = MOEXRequests.makeBondLastPriceURLForMOEX (primaryBoard, ticker);
        
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
             : amounts.size () == 1 && offers.isEmpty () ? CouponValueMode.FIXED 
             : CouponValueMode.NOT_FIXED;
    }
    
    public long getYearsToEnd () {
        return end == null ? 0L : now.until (end, ChronoUnit.YEARS);
    }
    
    public long getMonthToEnd () {
        return end == null ? 0L : now.until (end, ChronoUnit.MONTHS);
    }
    
    public long getDaysToCoupon () {
        return nextCoupon == null ? Long.MAX_VALUE : now.until (nextCoupon, ChronoUnit.DAYS);
    }
    
    private double couponsCredit, nominalValueWithInflation, pureCredit;
    private boolean reliableCoupons;
    private double score = 0.0;
    
    public void updateScore (ITBSProfile profile, Map <Currency, Double> rub2cur2coef) {
        final var currencyCoeff = rub2cur2coef.getOrDefault (currency, 1.0);
        final var months = now.until (end, ChronoUnit.MONTHS);
        final var days = now.until (end, ChronoUnit.DAYS);
        
        reliableCoupons = coupons.stream ().map (Coupon::isReliable).reduce (Boolean::logicalAnd).orElse (true);
        couponsCredit = coupons.stream ().mapToDouble (c -> c.getCredit (profile, now, end)).sum ();
        
        final var inflationFactor =  Math.pow (1 + profile.getInflation (), days / 365.0);        
        pureCredit = (nominalValue - lastPrice) / inflationFactor + couponsCredit;
        nominalValueWithInflation = nominalValue / inflationFactor;
        
        final var priceBalance = profile.getSafeMaxPrice (lastPrice) - lastPrice;
        final var monthsBalance = months - profile.getSafeMinMonths ();
        score = pureCredit * currencyCoeff + monthsBalance * 1.13 + priceBalance * currencyCoeff * 1.35 
              - lots * 0.25 + couponsPerYear * 0.25 + percentage * 1.4 - 200.0;
        score *= nominalValue != 0.0 ? 1000.0 / nominalValue : 1.0; // align to 1k nominal
    }
    
}
