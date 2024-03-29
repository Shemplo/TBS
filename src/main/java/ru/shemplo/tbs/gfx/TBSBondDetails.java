package ru.shemplo.tbs.gfx;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import com.panemu.tiwulfx.control.NumberField;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ru.shemplo.tbs.Pair;
import ru.shemplo.tbs.RollingAverage;
import ru.shemplo.tbs.TBSBackgroundExecutor;
import ru.shemplo.tbs.TBSBondDetailsManager;
import ru.shemplo.tbs.TBSClient;
import ru.shemplo.tbs.TBSEmitterManager;
import ru.shemplo.tbs.TBSLogWrapper;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.Bond;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.Coupon;
import ru.shemplo.tbs.entity.ICoupon;
import ru.shemplo.tbs.entity.IEmitter;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.gfx.component.TileWithHeader;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;

public class TBSBondDetails extends VBox {
    
    private NumberField <Long> lotsInPortfolio, lotsIssued, lotSize, daysToOffer, daysToCoupon;
    private NumberField <Double> nominalValue, lotValue, score, price, couponsTotal, couponsInflation;
    private NumberField <Integer> offers;
    private TableView <ICoupon> coupons;
    private TextField ticker, figi, emitter, start, end, duration, offer;
    private ImageView emitterRating;
    private Text titleName, latinName, updatedDate;
    private Tab tab;
    private XYChart <String, Number> priceChart;
    
    private IProfile profile;
    private Bond bond;
    
    private boolean couponsTableInitialized = false;
    
    public TBSBondDetails (Tab tab, Bond bond, IProfile profile) {
        this.profile = profile;
        this.bond = bond;
        this.tab = tab;
        
        tab.setText (bond.getCode ());
        makeLayout (bond.getCode ());
        updateLayout (bond);
    }
    
    public TBSBondDetails (Tab tab, IProfile profile, String ticker) {
        this.profile = profile;
        this.tab = tab;
        
        makeLayout (ticker);
        
        TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
            final var log = new TBSLogWrapper ();                
            try {
                final var bond = loadBondDetailsByTicker (profile, ticker, log);
                TBSBondDetailsManager.getInstance ().addDetailed (profile, bond);
                this.bond = bond;
                
                updateLayout (bond);
            } catch (ApiRuntimeException apire) {
                if ("50002".equals (apire.getCode ())) {
                    // Bond not found
                } else {
                    log.error ("Failed to load bond details", apire);                    
                }
            } catch (IOException ioe) {
                log.error ("Failed to load bond details", ioe);
            }
        });
    }
    
    private void makeLayout (String ticker) {
        setPadding (new Insets (12, 16.0, 12.0, 16.0));
        
        final var mainColumn = new VBox ();
        mainColumn.setFillWidth (true);
        
        final var scroller = new ScrollPane (mainColumn);
        scroller.setPadding (new Insets (0.0, 4.0, 0.0, 0.0));
        scroller.setFitToHeight (true);
        scroller.setFitToWidth (true);
        scroller.setBackground (null);
        getChildren ().add (scroller);
        
        final var row = new HBox (8.0);
        mainColumn.getChildren ().add (row);
        
        // Left column
        
        final var columnLeft = new VBox (8.0);
        HBox.setMargin (columnLeft, new Insets (0.0, 16.0, 0.0, 0.0));
        HBox.setHgrow (columnLeft, Priority.ALWAYS);
        row.getChildren ().add (columnLeft);
        
        final var titleRow = new HBox (8.0);
        titleRow.setAlignment (Pos.BASELINE_LEFT);
        columnLeft.getChildren ().add (titleRow);
        
        titleName = new Text ();
        titleName.setFont (TBSStyles.FONT_BOLD_20);
        titleRow.getChildren ().add (titleName);
        
        final var titleSeparator = new HBox ();
        HBox.setHgrow (titleSeparator, Priority.ALWAYS);
        titleRow.getChildren ().add (titleSeparator);
        
        final var reloadButton = new Button ("Update data");
        reloadButton.setPadding (new Insets (4.0, 8.0, 4.0, 8.0));
        reloadButton.setOnAction (ae -> reloadBond ());
        reloadButton.setFocusTraversable (false);
        titleRow.getChildren ().add (reloadButton);
        
        final var underTitleRow = new HBox (8.0);
        columnLeft.getChildren ().add (underTitleRow);
        
        latinName = new Text ();
        latinName.setFont (TBSStyles.FONT_NORMAL_16);
        underTitleRow.getChildren ().add (latinName);
        
        final var underTitleSeparator = new HBox ();
        HBox.setHgrow (underTitleSeparator, Priority.ALWAYS);
        underTitleRow.getChildren ().add (underTitleSeparator);
        
        updatedDate = new Text ();
        underTitleRow.getChildren ().add (updatedDate);
        
        final var priceAxis = new NumberAxis ();
        priceAxis.setForceZeroInRange (false);
        
        priceChart = new LineChart <> (new CategoryAxis (), priceAxis);
        columnLeft.getChildren ().add (priceChart);
        priceChart.setLegendSide (Side.TOP);
        priceChart.setAnimated (false);
        
        final var originalPriceSeries = new Series <String, Number> ();
        originalPriceSeries.setName ("Original price");
        priceChart.getData ().add (originalPriceSeries);
        
        final var averagePriceSeries = new Series <String, Number> ();
        averagePriceSeries.setName ("Rolling average price");
        priceChart.getData ().add (averagePriceSeries);
        
        // Right column
        
        final var columnRight = new VBox (20.0);
        row.getChildren ().add (columnRight);
        
        final var codesRow = new HBox (8.0);
        columnRight.getChildren ().add (codesRow);
        
        codesRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Ticker", this.ticker = TBSUIUtils.declareCustomized (
                new TextField (), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        codesRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("FIGI", figi = TBSUIUtils.declareCustomized (
                new TextField (), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        codesRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Class code", TBSUIUtils.declareCustomized (
                new TextField ("TQCB"), tf -> defaultTextFiledCustomizer (tf, 122.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        final var emitterRow = new HBox (8.0);
        emitterRow.setAlignment (Pos.BOTTOM_LEFT);
        columnRight.getChildren ().add (emitterRow);
        
        emitter = new TextField ();       
        emitter.setEditable (false);
        emitter.setMinWidth (474.0);
        emitterRow.getChildren ().add (new TileWithHeader <> ("Emitter", emitter));
        
        emitterRating = new ImageView ();
        HBox.setMargin (emitterRating, new Insets (0.0, 0.0, 2.0, 0.0));
        emitterRating.setFitHeight (24.0);
        emitterRating.setFitWidth (24.0);
        emitterRow.getChildren ().add (emitterRating);
        
        final var scoreRow = new HBox (8.0);
        columnRight.getChildren ().add (scoreRow);
        
        scoreRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Score", score = TBSUIUtils.declareCustomized (
                new NumberField <> (Double.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        scoreRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Last market price / lot", price = TBSUIUtils.declareCustomized (
                new NumberField <> (Double.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        final var lotsRow = new HBox (8.0);
        columnRight.getChildren ().add (lotsRow);
        
        lotsRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Lots in portfolio", lotsInPortfolio = TBSUIUtils.declareCustomized (
                new NumberField <> (Long.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        lotsRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Lots issued", lotsIssued = TBSUIUtils.declareCustomized (
                new NumberField <> (Long.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        final var portfolioLotsPercentage = new NumberField <> (Number.class);
        portfolioLotsPercentage.setEditable (false);
        portfolioLotsPercentage.setMaxWidth (122.0);
        portfolioLotsPercentage.setDigitBehindDecimal (8);
        lotsRow.getChildren ().add (new TileWithHeader <> ("Portfolio %", portfolioLotsPercentage));
        
        final var liv = TBSUtils.wrapToNumberValue (lotsIssued.valueProperty ());
        final var lipv = TBSUtils.wrapToNumberValue (lotsInPortfolio.valueProperty ());
        portfolioLotsPercentage.valueProperty ().bind (Bindings.divide (lipv, liv).multiply (100.0));
        
        final var lotsRow2 = new HBox (8.0);
        columnRight.getChildren ().add (lotsRow2);
        
        lotsRow2.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Nominal value", nominalValue = TBSUIUtils.declareCustomized (
                new NumberField <> (Double.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        lotsRow2.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Lot value", lotValue = TBSUIUtils.declareCustomized (
                new NumberField <> (Double.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        lotsRow2.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Lot size / operation", lotSize = TBSUIUtils.declareCustomized (
                new NumberField <> (Long.class), tf -> defaultTextFiledCustomizer (tf, 122.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        final var datesRow = new HBox (8.0);
        columnRight.getChildren ().add (datesRow);
        
        datesRow.getChildren ().add (TBSUIUtils.declareCustomized (
                new TileWithHeader <> ("Start date", start = TBSUIUtils.declareCustomized (
                    new TextField (), tf -> defaultTextFiledCustomizer (tf, 180.0)
                )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        datesRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("End date", end = TBSUIUtils.declareCustomized (
                new TextField (), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        datesRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Duration", duration = TBSUIUtils.declareCustomized (
                new TextField (), tf -> defaultTextFiledCustomizer (tf, 122.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        final var offersRow = new HBox (8.0);
        columnRight.getChildren ().add (offersRow);
        
        offersRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Next offer", offer = TBSUIUtils.declareCustomized (
                new TextField (), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, true)
        ));
        
        offersRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Days to next offer", daysToOffer = TBSUIUtils.declareCustomized (
                new NumberField <> (Long.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        offersRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Total offers", offers = TBSUIUtils.declareCustomized (
                new NumberField <> (Integer.class), tf -> defaultTextFiledCustomizer (tf, 122.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        final var couponsRow = new HBox (8.0);
        columnRight.getChildren ().add (couponsRow);
        
        couponsRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Coupons (total)", couponsTotal = TBSUIUtils.declareCustomized (
                new NumberField <> (Double.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        couponsRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Coupons (infalted)", couponsInflation = TBSUIUtils.declareCustomized (
                new NumberField <> (Double.class), tf -> defaultTextFiledCustomizer (tf, 180.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        couponsRow.getChildren ().add (TBSUIUtils.declareCustomized (
            new TileWithHeader <> ("Days to next coupon", daysToCoupon = TBSUIUtils.declareCustomized (
                new NumberField <> (Long.class), tf -> defaultTextFiledCustomizer (tf, 122.0)
            )), 
            tile -> defaultTileCustomizer (tile, false)
        ));
        
        coupons = new TableView <ICoupon> ();
        initializeCouponsTable (coupons, bond);
        columnRight.getChildren ().add (coupons);
    }
    
    private void defaultTextFiledCustomizer (TextField field, double width) {
        field.setEditable (false);
        if (width > 150.0) {
            field.setMinWidth (width);
        } else {
            field.setMaxWidth (width);
        }
    }
    
    private void defaultTileCustomizer (TileWithHeader <?> tile, boolean accent) {
        tile.setBackground (accent ? TBSStyles.BG_TILE_ACCENT : null);
        tile.setPadding (new Insets (4.0, 2.0, 2.0, 2.0));
    }
    
    private void initializeCouponsTable (TableView <ICoupon> table, Bond bond) {
        if (couponsTableInitialized || table == null || bond == null) { return; }
        
        TBSCouponsInspection.initializeTable (table, bond);
        table.setFixedCellSize (24.0);
        final var couponsAmount = Bindings.size (table.getItems ()).add (1.15);
        table.prefHeightProperty ().bind (table.fixedCellSizeProperty ().multiply (couponsAmount));
        table.minHeightProperty ().bind (table.prefHeightProperty ());
        table.maxHeightProperty ().bind (table.prefHeightProperty ());
        couponsTableInitialized = true;
    }
    
    private void updateLayout (Bond bond) {
        final var emitterManager = TBSEmitterManager.getInstance ();
        final var ticker = bond.getCode ();
        
        Platform.runLater (() -> {
            tab.setText (String.format ("[ %s ] %s", ticker, bond.getName ()));
            updatedDate.setText ("Last update: " + TBSUtils.mapIfNN (bond.getLastUpdated (), Date::toString, "(unknown)"));
            latinName.setText (bond.getLatinName ());
            titleName.setText (bond.getName ());
            
            final var originalPriceData = priceChart.getData ().get (0).getData ();
            originalPriceData.clear ();
            
            final var averagePriceData = priceChart.getData ().get (1).getData ();
            averagePriceData.clear ();
            
            final var originalPrice = TBSUtils.aOrB (bond.getHistoricalPrice (), List.<Pair <Date, Double>> of ());
            final var average = new RollingAverage (7, false).addSequence (originalPrice.stream ().map (Pair::b).toList ()).finish ();
            
            for (int i = Math.max (0, originalPrice.size () - 30); i < originalPrice.size (); i++) {
                final var dateNprice = originalPrice.get (i);
                double value = dateNprice.b (), averageValue = average.get (i);
                
                final var date = LocalDate.from (dateNprice.a ().toInstant ().atZone (ZoneId.systemDefault ()));
                averagePriceData.add (new Data <> (date.toString (), averageValue));
                originalPriceData.add (new Data <> (date.toString (), value));
            }
            
            this.ticker.setText (bond.getCode ());
            figi.setText (bond.getFigi ());
            
            final var emitter = emitterManager.getEmitterById (bond.getEmitterId ());
            final var emitterRating = TBSUtils.mapIfNN (emitter, IEmitter::getRating, BondCreditRating.UNDEFINED);
            this.emitter.setText (TBSUtils.mapIfNN (emitter, IEmitter::getName, "(unknown)"));
            this.emitterRating.setImage (emitterRating.getIcon ());
            
            price.setValue (bond.getLastPrice ());
            score.setValue (bond.getScore ());
            
            nominalValue.setValue (bond.getNominalValue ());
            lotsIssued.setValue (bond.getLotsIssued ());
            lotsInPortfolio.setValue (bond.getLots ());
            lotValue.setValue (bond.getLotValue ());
            lotSize.setValue (bond.getLotSize ());
            
            duration.setText (bond.getStart ().until (bond.getEnd (), ChronoUnit.MONTHS) + " months");
            start.setText (bond.getStart ().toString ());
            end.setText (bond.getEnd ().toString ());
            
            final var nextOffer = bond.getOffers ().ceiling (bond.getNow ());
            daysToOffer.setValue (TBSUtils.mapIfNN (nextOffer, no -> bond.getNow ().until (nextOffer, ChronoUnit.DAYS), -1L));
            offer.setText (TBSUtils.mapIfNN (nextOffer, LocalDate::toString, "(no offers)"));
            offers.setValue (bond.getOffers ().size ());
            
            couponsTotal.setValue (bond.getCoupons ().stream ().mapToDouble (Coupon::getAmount).sum ());
            couponsInflation.setValue (bond.getCouponsCredit ());
            daysToCoupon.setValue (bond.getDaysToCoupon ());
            
            initializeCouponsTable (coupons, bond);
            coupons.getItems ().setAll (bond.getCoupons ());
        });
    }
    
    private void reloadBond () {
        TBSBackgroundExecutor.getInstance ().runInBackground (() -> {
            bond.reloadAndUpdateScore (profile, true);
            updateLayout (bond);
            
            TBSBondDetailsManager.getInstance ().dump (profile);
        });
    }
    
    private Bond loadBondDetailsByTicker (IProfile profile, String ticker, TBSLogWrapper log) throws IOException {
        final var client = TBSClient.getInstance ().getConnection (profile, log);
        
        final var raw = client.getInstrumentsService ().getBondByTickerSync (ticker, "TQCB");
        
        final var accounts = client.getUserService ().getAccountsSync ();
        final var lots = client.getOperationsService ()
            . getPortfolioSync (accounts.get (0).getId ()).getPositions ().stream ()
            . filter (pos -> pos.getFigi ().equals (raw.getFigi ()))
            . map (pos -> pos.getQuantity ().longValue ())
            . findFirst ().orElse (0L);
        
        final var bond = new Bond (profile, raw, true);
        bond.updateScore (profile);
        bond.setLots (lots);
        return bond;
    }
    
}
