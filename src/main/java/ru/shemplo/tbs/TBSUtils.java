package ru.shemplo.tbs;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import ru.shemplo.tbs.entity.BondCreditRating;
import ru.shemplo.tbs.entity.Currency;

public class TBSUtils {
    
    public static <F, S> List <S> mapToList (Collection <F> values, Function <F, S> converter) {
        return values.stream ().map (converter).collect (Collectors.toList ());
    }
    
    public static <F> F aOrB (F a, F b) {
        return a == null ? b : a;
    }
    
    public static <F, S> S mapIfNN (F value, Function <F, S> converter, S defaultValue) {
        return aOrB (value == null ? null : converter.apply (value), defaultValue);
    }
    
    public static <F, S, T> T mapIf2NN (F value, S value2, BiFunction <F, S, T> converter, T defaultValue) {
        return aOrB (value == null && value2 != null ? null : converter.apply (value, value2), defaultValue);
    }
    
    public static <F, S, T> T map2IfNN (F value, Function <F, S> converter, Function <S, T> converter2, T defaultValue) {
        final var s = value == null ? null : converter.apply (value);
        return aOrB (s == null ? null : converter2.apply (s), defaultValue);
    }
    
    public static <F> void doIfNN (F value, Consumer <F> action) {
        if (value != null) { action.accept (value); }
    }
    
    public static <F, S> void doIf2NN (F value, S value2, BiConsumer <F, S> action) {
        if (value != null && value2 != null) { 
            action.accept (value, value2); 
        }
    }
    
    public static boolean notBlank (String string) {
        return string != null && !string.isBlank ();
    }
    
    public static Optional <Currency> getCurrencyByTicker (String ticker) {
        final var currency = switch (ticker) {
            case "USD000UTSTOM" -> Currency.USD;
            case "EUR_RUB__TOM" -> Currency.EUR;
            default -> null;
        };
        
        return Optional.ofNullable (currency);
    }
    
    public static Optional <BondCreditRating> fetchCreditRating (String rating) {
        final var r = switch (rating.toLowerCase ()) {
            case "высокий" -> BondCreditRating.HIGH;
            case "умеренный" -> BondCreditRating.MEDIUM;
            case "низкий" -> BondCreditRating.LOW;
            default -> null;
        };
        
        return Optional.ofNullable (r);
    }
    
    public static <F, S> void bindBidirectionalMapping (
        Property <F> f, Property <S> s, 
        Throwing3Function <F, S, Boolean, S, RuntimeException> f2s, 
        Throwing3Function <S, F, Boolean, F, RuntimeException> s2f
    ) {
        final var lockF = new AtomicReference <> (f.getValue ());
        final var lockS = new AtomicReference <> (s.getValue ());
        
        final var listenerF = (ChangeListener <F>) (__, ___, fv) -> {
            System.out.println (String.format (
                "New F: %s, S: %s, lock F: %s, lock S: %s", 
                fv, s.getValue (), lockF.get (), lockS.get ()
            )); // SYSOUT
            if (!Objects.equals (lockS.get (), f2s.apply (fv, s.getValue (), false))) {                
                lockS.set (f2s.apply (fv, s.getValue (), true));
                s.setValue (lockS.get ());
            }
        };
        final var listenerS = (ChangeListener <S>) (__, ___, sv) -> {
            System.out.println (String.format (
                "F: %s, New S: %s, lock F: %s, lock S: %s", 
                f.getValue (), sv, lockF.get (), lockS.get ()
            )); // SYSOUT
            if (!Objects.equals (lockF.get (), s2f.apply (sv, f.getValue (), false))) {                
                lockF.set (s2f.apply (sv, f.getValue (), true));
                f.setValue (lockF.get ());
            }
        };
        
        f.addListener (listenerF);
        s.addListener (listenerS);
    }
    
}
