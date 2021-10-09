package ru.shemplo.tbs;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    
    public static <F> void doIfNN (F value, Consumer <F> action) {
        if (value != null) { action.accept (value); }
    }
    
}
