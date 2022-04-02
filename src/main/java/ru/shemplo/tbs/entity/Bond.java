package ru.shemplo.tbs.entity;

import static ru.shemplo.tbs.TBSConstants.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.shemplo.tbs.TBSCurrencyManager;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.moex.MOEXRequests;
import ru.shemplo.tbs.moex.MOEXResposeReader;
import ru.shemplo.tbs.moex.xml.Data;
import ru.shemplo.tbs.moex.xml.Row;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;

@Getter
@ToString
public class Bond extends AbstractObservableEntity <IBond> implements IBond {
    
    private static final long serialVersionUID = 1L;
    
    private String name, code, figi;
    private Currency currency;
    
    @Setter
    private long lots = 0;
    
    private LocalDate start, end, nextCoupon, nextRecord;
    private long couponsPerYear;
    private LocalDate now;
    
    private long emitterId;
    
    private double nominalValue, percentage, lastPrice;
    
    private NavigableSet <LocalDate> offers = new TreeSet <> ();
    private List <Coupon> coupons = new ArrayList <> ();
    
    private String primaryBoard;
    
    public Bond (ru.tinkoff.piapi.contract.v1.Bond instrument) {
        this (instrument.getTicker (), instrument.getFigi (), Currency.valueOf (instrument.getCurrency ()), NOW, 0);
    }
    
    public Bond (String ticker, Currency currency, PortfolioPosition portfolio) {
        this (ticker, portfolio.getFigi (), currency, FAR_PAST, portfolio.getQuantityLots ().getUnits ());
    }
    
    private Bond (String ticker, String figi, Currency currency, LocalDate scoreNow, long lots) {
        this.currency = currency;
        this.now = scoreNow;
        this.figi = figi;
        this.lots = lots;
        
        final var MOEX_DESCRIPION_URL = MOEXRequests.makeBondDescriptionURLForMOEX (ticker);
        final var MOEX_COUPONS_URL = MOEXRequests.makeBondCouponsURLForMOEX (ticker);
        
        if (MOEX_DESCRIPION_URL != null) {
            final var MOEXData = MOEXResposeReader.read (MOEX_DESCRIPION_URL);
            MOEXData.getDescription ().ifPresent (description -> {
                name = description.getBondName ().orElse ("");
                code = description.getBondCode ().orElse ("");
                
                couponsPerYear = description.getBondCouponsPerYear ().orElse (1);
                //nextCoupon = description.getBondNextCouponDate ().orElse (null);
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
                Coupon previous = new Coupon (FAR_PAST, FAR_PAST, 0.0, true, "");
                
                for (final var coupon : Optional.ofNullable (cops.getRows ()).orElse (List.of ())) {
                    coupons.add (previous = new Coupon (coupon, previous, offers, now));
                    if (previous.isNextCoupon ()) { 
                        nextRecord = previous.getRecord ();
                        nextCoupon = previous.getDate (); 
                    }
                }
            });
        }
        
        final var MOEX_PRICE_URL = MOEXRequests.makeBondLastPriceURLForMOEX (primaryBoard, ticker);
        
        if (MOEX_PRICE_URL != null) {
            final var MOEXPrice = MOEXResposeReader.read (MOEX_PRICE_URL);
            MOEXPrice.getMarketData ().map (Data::getRows).ifPresent (price -> {
                final var lastValue = price.getRows () == null || price.getRows ().isEmpty () ? ""
                                    : price.getRows ().get (0).getPrice ();
                lastPrice = lastValue.isBlank () ? 1e+9 : (Double.parseDouble (lastValue) * nominalValue / 100.0);
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
    
    public long getMonthsToEnd () {
        return end == null ? 0L : now.until (end, ChronoUnit.MONTHS);
    }
    
    public long getDaysToCoupon () {
        return nextCoupon == null ? Long.MAX_VALUE : now.until (nextCoupon, ChronoUnit.DAYS);
    }
    
    private double couponsCredit, nominalValueWithInflation, pureCredit;
    private boolean reliableCoupons;
    private double score = 0.0;
    
    public void updateScore (IProfile profile) {
        final var currencyCoeff = TBSCurrencyManager.getInstance ().getToRubCoefficient (currency);
        final var months = now.until (end, ChronoUnit.MONTHS);
        final var days = now.until (end, ChronoUnit.DAYS);
        
        reliableCoupons = coupons.stream ().map (Coupon::isReliable).reduce (Boolean::logicalAnd).orElse (true);
        couponsCredit = coupons.stream ().mapToDouble (c -> c.getCredit (profile, now, end)).sum ();
        
        final var inflationFactor =  Math.pow (1 + profile.getInflation (), days / 365.0);        
        nominalValueWithInflation = nominalValue / inflationFactor;
        pureCredit = nominalValueWithInflation + couponsCredit - lastPrice;
        //pureCredit = (nominalValue - lastPrice) / inflationFactor + couponsCredit;
        
        final var priceBalance = profile.getSafeMaxPrice (lastPrice) - lastPrice;
        final var monthsBalance = months - profile.getSafeMinMonths ();
        score = pureCredit * currencyCoeff /*/ (months == 0 ? 1000 : months)*/ * 1.61 - Math.sqrt (monthsBalance) * 0.34 
              + priceBalance * currencyCoeff * 1.08 - lots * 0.25 + couponsPerYear * 0.15 + percentage * 1.09;
        score *= nominalValue != 0.0 ? 1000.0 / nominalValue : 1.0; // align to 1k nominal
        score *= 1.0 - TBSEmitterManager.getCreditRating (emitterId).getPenalty ();
        score = Math.signum (score) * Math.sqrt (Math.abs (score)) - 7.0;
    }
    
}
