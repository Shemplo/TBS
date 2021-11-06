package ru.shemplo.tbs.entity;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import javafx.beans.property.ObjectProperty;
import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.shemplo.tbs.ObservableEntityProxy;

@RequiredArgsConstructor
public abstract class AbstractObservableEntity <I> implements ObservableEntity <I> {
    
    @Setter (value = AccessLevel.PRIVATE)
    protected transient volatile Pair <I, ObservableEntityProxy <AbstractObservableEntity <I>>> proxyNhandler;
    
    @SuppressWarnings ("unchecked")
    protected Pair <I, ObservableEntityProxy <AbstractObservableEntity <I>>> makeProxy () {
        final var handler = new ObservableEntityProxy <> (this);
        final var infs = getClass ().getInterfaces ();
        final var cl = getClass ().getClassLoader ();
        
        return new Pair <> ((I) Proxy.newProxyInstance (cl, infs, handler), handler);
    }
    
    protected Pair <I, ObservableEntityProxy <AbstractObservableEntity <I>>> ensureProxyInitialized () {
        if (proxyNhandler == null) {
            synchronized (this) {
                if (proxyNhandler == null) {
                    proxyNhandler = makeProxy ();
                }
            }
        }   
        
        return proxyNhandler;
    }
    
    @Override
    public I getProxy () {
        return ensureProxyInitialized ().getKey ();
    }
    
    @Override
    public <V> ObjectProperty <V> getProperty (String field, Supplier <V> defaultValue, boolean applyReadWrite) {
        return ensureProxyInitialized ().getValue ().getProperty (field, defaultValue, applyReadWrite);
    }
    
}
