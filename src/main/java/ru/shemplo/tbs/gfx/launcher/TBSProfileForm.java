package ru.shemplo.tbs.gfx.launcher;

import java.util.Set;
import java.util.function.BiConsumer;

import com.panemu.tiwulfx.control.NumberField;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import ru.shemplo.tbs.TBSUtils;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.CouponValueMode;
import ru.shemplo.tbs.entity.Currency;
import ru.shemplo.tbs.entity.IProfile;
import ru.shemplo.tbs.entity.Range;
import ru.shemplo.tbs.gfx.TBSApplicationIcons;
import ru.shemplo.tbs.gfx.component.EnumSelectionSet;
import ru.shemplo.tbs.gfx.component.RangeFields;
import ru.shemplo.tbs.gfx.component.SliderWithField;
import ru.shemplo.tbs.gfx.component.TileWithHeader;

public class TBSProfileForm {
    
    @Setter
    private BiConsumer <IProfile, Boolean> onSaveRequest;
    
    public TBSProfileForm (Window window, IProfile profile, boolean isNew) {        
        final var root = new VBox ();
        root.setPadding (new Insets (8.0));
        
        final var scene = new Scene (root);
        
        final var stage = new Stage ();
        root.getChildren ().add (makeGeneralSection (profile, isNew, stage));
        
        stage.setTitle (String.format ("Tinkoff Bonds Scanner | Launcher | Profile form"));
        stage.getIcons ().add (TBSApplicationIcons.window);
        stage.initModality (Modality.WINDOW_MODAL);
        stage.setResizable (false);
        stage.initOwner (window);
        stage.setScene (scene);
        stage.sizeToScene ();
        stage.show ();
    }
    
    private Parent makeGeneralSection (IProfile profile, boolean isNew, Stage stage) {
        final var column = new VBox (8.0);
        
        // General
        
        final var generalLine = new HBox (8.0);
        column.getChildren ().add (generalLine);
        
        final var name = new TileWithHeader <> ("Profile name:", new TextField ());
        name.getNodes () [0].setEditable (profile.isEditable ());
        name.getNodes () [0].setText (profile.name ());
        name.setMinWidth (310.0);
        generalLine.getChildren ().add (name);
                
        final var maxResults = new TileWithHeader <> ("Max results:", new NumberField <> (Integer.class));
        maxResults.getNodes () [0].setValue ((int) profile.getMaxResults ());
        maxResults.getNodes () [0].setEditable (profile.isEditable ());
        maxResults.setMaxWidth (80.0);
        generalLine.getChildren ().add (maxResults);
        
        final var inflation = new TileWithHeader <> ("Inflation: ", new SliderWithField <> (
            Double.class, 0.0, 15.0, profile.getInflation () * 100
        ));
        final var inflationSlider = ((SliderWithField <Double>) inflation.getNodes () [0]);
        inflationSlider.setDisable (!profile.isEditable ());
        inflationSlider.getSlider ().setMajorTickUnit (3.0);
        inflationSlider.getSlider ().setMinorTickCount (5);
        inflation.setMaxWidth (400.0);
        column.getChildren ().add (inflation);
        
        // Optional parameters
        
        final var monthsTillEnd = new TileWithHeader <> ("Months till end:", new RangeFields <> (Integer.class));
        monthsTillEnd.getNodes () [0].setMin (TBSUtils.mapIfNN (profile.getMonthsTillEnd (), Range::getMin, null));
        monthsTillEnd.getNodes () [0].setMax (TBSUtils.mapIfNN (profile.getMonthsTillEnd (), Range::getMax, null));
        monthsTillEnd.setMinWidth (400.0);
        column.getChildren ().add (monthsTillEnd);
        
        final var daysToCoupon = new TileWithHeader <> ("Days till coupon:", new RangeFields <> (Integer.class));
        daysToCoupon.getNodes () [0].setMin (TBSUtils.mapIfNN (profile.getDaysToCoupon (), Range::getMin, null));
        daysToCoupon.getNodes () [0].setMax (TBSUtils.mapIfNN (profile.getDaysToCoupon (), Range::getMax, null));
        column.getChildren ().add (daysToCoupon);
        
        final var couponsPerYear = new TileWithHeader <> ("Coupons per year:", new RangeFields <> (Integer.class));
        couponsPerYear.getNodes () [0].setMin (TBSUtils.mapIfNN (profile.getCouponsPerYear (), Range::getMin, null));
        couponsPerYear.getNodes () [0].setMax (TBSUtils.mapIfNN (profile.getCouponsPerYear (), Range::getMax, null));
        column.getChildren ().add (couponsPerYear);
        
        final var nominal = new TileWithHeader <> ("Nominal value (RUB):", new RangeFields <> (Double.class));
        nominal.getNodes () [0].setMin (TBSUtils.mapIfNN (profile.getNominalValue (), Range::getMin, null));
        nominal.getNodes () [0].setMax (TBSUtils.mapIfNN (profile.getNominalValue (), Range::getMax, null));
        column.getChildren ().add (nominal);
        
        final var percentage = new TileWithHeader <> ("Priori percentage:", new RangeFields <> (Double.class));
        percentage.getNodes () [0].setMin (TBSUtils.mapIfNN (profile.getPercentage (), Range::getMin, null));
        percentage.getNodes () [0].setMax (TBSUtils.mapIfNN (profile.getPercentage (), Range::getMax, null));
        column.getChildren ().add (percentage);
        
        final var price = new TileWithHeader <> ("Committed price (RUB):", new RangeFields <> (Double.class));
        price.getNodes () [0].setMin (TBSUtils.mapIfNN (profile.getPrice (), Range::getMin, null));
        price.getNodes () [0].setMax (TBSUtils.mapIfNN (profile.getPrice (), Range::getMax, null));
        column.getChildren ().add (price);
        
        // Currencies
        
        final var currencies = new TileWithHeader <> ("Currencies:", new EnumSelectionSet <> (
            Currency.class, TBSUtils.aOrB (profile.getCurrencies (), Set.of ())
        ));
        currencies.setMaxWidth (400.0);
        column.getChildren ().add (currencies);
        
        // Credit rating
        
        final var creditRating = new TileWithHeader <> ("Bond credit rating:", new EnumSelectionSet <> (
            BondCreditRating.class, TBSUtils.aOrB (profile.getCreditRatings (), Set.of ())
        ));
        creditRating.setMaxWidth (400.0);
        column.getChildren ().add (creditRating);
        
        // Coupon modes
        
        final var couponModes = new TileWithHeader <> ("Coupon modes:", new EnumSelectionSet <> (
            CouponValueMode.class, TBSUtils.aOrB (profile.getCouponValuesModes (), Set.of ())
        ));
        couponModes.setMaxWidth (400.0);
        column.getChildren ().add (couponModes);
                
        // Token
        
        final var tokenLineHeaders = new HBox (8.0);
        column.getChildren ().add (tokenLineHeaders);
        
        final var tokenHeader = new Text ("API token:");
        tokenLineHeaders.getChildren ().addAll (tokenHeader);
        
        final var tokenLine = new HBox (8.0);
        tokenLine.setAlignment (Pos.BASELINE_LEFT);
        column.getChildren ().add (tokenLine);
        
        final var tokenField = new TextField ();
        tokenField.setText (profile.getToken ());
        tokenField.setMinWidth (308.0);
        tokenLine.getChildren ().addAll (tokenField);
        
        final var tokenResponsibilityCheck = new CheckBox ("Production");
        tokenResponsibilityCheck.setSelected (profile.isHighResponsible ());
        tokenLine.getChildren ().addAll (tokenResponsibilityCheck);
        
        // Save line
        
        final var saveLine = new HBox (8.0);
        saveLine.setAlignment (Pos.CENTER_LEFT);
        column.getChildren ().add (saveLine);
        
        final var save = new Button ("Save");
        saveLine.getChildren ().add (save);
        
        final var warningIcon = new ImageView (TBSApplicationIcons.warning24);
        final var errorProperty = warningIcon.visibleProperty ();
        warningIcon.setFitHeight (20);
        warningIcon.setFitWidth (20);
        errorProperty.set (false);
        saveLine.getChildren ().add (warningIcon);        
        
        final var warningMessage = new Text ();
        warningMessage.visibleProperty ().bind (errorProperty);
        saveLine.getChildren ().add (warningMessage);
        
        save.setOnMouseClicked (me -> {
            if (me.getButton () != MouseButton.PRIMARY || me.getClickCount () > 1) {
                return;
            }
            
            if (onSaveRequest != null) {
                final var nameValue = name.getNodes () [0].getText ();
                if (nameValue == null || nameValue.isBlank ()) {
                    warningMessage.setText ("Profile name should be defined (not empty, not only spaces)");
                    errorProperty.set (true);
                    return;
                }
                
                final var tokenValue = tokenField.getText ();
                if (tokenValue == null || tokenValue.isBlank ()) {
                    warningMessage.setText ("API token should be defined");
                    errorProperty.set (true);
                    return;
                }
                
                profile.setInflation (inflation.getNodes () [0].getValueProperty ().get () / 100.0);
                profile.setMaxResults (maxResults.getNodes () [0].getValue ());
                profile.setName (nameValue);
                
                profile.setCouponsPerYear (couponsPerYear.getNodes () [0].getRange ());
                profile.setMonthsTillEnd (monthsTillEnd.getNodes () [0].getRange ());
                profile.setDaysToCoupon (daysToCoupon.getNodes () [0].getRange ());
                profile.setPercentage (percentage.getNodes () [0].getRange ());
                profile.setNominalValue (nominal.getNodes () [0].getRange ());
                profile.setPrice (price.getNodes () [0].getRange ());
                
                profile.setCouponValuesModes (couponModes.getNodes () [0].getOptions ());
                profile.setCreditRatings (creditRating.getNodes () [0].getOptions ());
                profile.setCurrencies (currencies.getNodes () [0].getOptions ());
                
                profile.setHighResponsible (tokenResponsibilityCheck.isSelected ());
                profile.setToken (tokenValue);
                
                onSaveRequest.accept (profile, isNew);
                errorProperty.set (false);
                stage.close ();
            }
        });
        
        return column;
    }
    
}
