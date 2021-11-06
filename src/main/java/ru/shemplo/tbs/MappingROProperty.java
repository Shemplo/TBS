package ru.shemplo.tbs;

import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class MappingROProperty <F, S> extends SimpleObjectProperty <S> {
    
    public MappingROProperty (Property <F> dependency, Function <F, S> mapper) {
        bind (Bindings.createObjectBinding (() -> mapper.apply (dependency.getValue ()), dependency));
    }
    
}
