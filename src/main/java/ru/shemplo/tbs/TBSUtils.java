package ru.shemplo.tbs;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.tinkoff.invest.openapi.model.rest.Currency;

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
    
    public static Optional <Currency> getCurrencyByTicker (String ticker) {
        final var currency = switch (ticker) {
            case "USD000UTSTOM" -> Currency.USD;
            case "EUR_RUB__TOM" -> Currency.EUR;
            default -> null;
        };
        
        return Optional.ofNullable (currency);
    }
    
}
