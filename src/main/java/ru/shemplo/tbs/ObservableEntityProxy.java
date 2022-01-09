package ru.shemplo.tbs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import javafx.beans.property.SimpleObjectProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.tbs.entity.ObservableEntity;
import ru.shemplo.tbs.entity.UpdateDateTracker;

@Slf4j
@RequiredArgsConstructor
public class ObservableEntityProxy <T> implements InvocationHandler {

    private final ConcurrentMap <String, SimpleObjectProperty <?>> field2property = new ConcurrentHashMap <> ();
    
    private final T instance;
    
    @Override
    public Object invoke (Object proxy, Method method, Object [] args) throws Throwable {
        final var methodName = method.getName ();
        
        if ("getRealObject".equals (methodName)) {
            return instance;
        } else if (methodName.startsWith ("set") && methodName.length () >= 4 && args.length == 1) {
            @SuppressWarnings ("unchecked")
            final var value = (T) args [0];
            
            final var fieldName = String.format ("%s%s", methodName.substring (3, 4).toLowerCase (), method.getName ().substring (4));
            final var property = getProperty (fieldName, () -> value, true); // initialize property if it not exists
            property.set (value);
            return null;
        }
        
        return method.invoke (instance, args);
    }
    
    public <V> SimpleObjectProperty <V> getProperty (String fieldName, Supplier <V> valueByDefault, boolean applyRW) {
        @SuppressWarnings ("unchecked")
        final var property = (SimpleObjectProperty <V>) field2property.computeIfAbsent (fieldName, __ -> {
            final var propName = String.join (".", instance.getClass ().getSimpleName (), fieldName);
            if (applyRW) {
                try {
                    final var field = instance.getClass ().getDeclaredField (fieldName);
                    
                    try {
                        field.setAccessible (true);
                        final var initialValue = TBSUtils.aOrB ((V) field.get (instance), 
                            TBSUtils.mapIfNN (valueByDefault, Supplier::get, null)
                        );
                        field.setAccessible (false);
                        
                        final var prop = new SimpleObjectProperty <V> (null, propName, initialValue);
                        
                        prop.addListener ((___, ____, value) -> {
                            try {
                                field.setAccessible (true);
                                field.set (instance, value);
                                field.setAccessible (false);
                                
                                if (!"upadted".equalsIgnoreCase (fieldName)) {
                                    final var type = instance.getClass ();
                                    if (UpdateDateTracker.class.isAssignableFrom (type)) {  
                                        if (ObservableEntity.class.isAssignableFrom (type)) {
                                            ((ObservableEntity <?>) instance).getRWProperty ("updated", () -> null).set (new Date ());
                                        } else {
                                            ((UpdateDateTracker) instance).updateNow ();
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException | IllegalAccessException e) {
                                // Do nothing if wrong value was tried to be written
                                e.printStackTrace ();
                            }
                        });
                        
                        return prop;
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // Do nothing if failed to read initial value
                        e.printStackTrace ();
                    }
                } catch (NoSuchFieldException | SecurityException e) {
                    // Do nothing if field does't exists
                    log.warn ("Unable to get properfy for undefined field `{}.{}`", 
                        instance.getClass ().getSimpleName (), fieldName
                    );
                }
            }
            
            return new SimpleObjectProperty <V> (null, propName, valueByDefault.get ());
        });
        
        return property;
    }
    
}
