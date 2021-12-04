package ru.shemplo.tbs.gfx.component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;

public class EnumSelectionSet <E extends Enum <E>> extends FlowPane {
    
    private final ObservableSet <E> options;
    
    public EnumSelectionSet (Class <E> enumeration) {
        this (enumeration, List.of ());
    }
    
    public EnumSelectionSet (Class <E> enumeration, Collection <E> selectedOptions) {
        setHgap (8.0); setVgap (4.0);
        
        options = FXCollections.observableSet (new HashSet <> (selectedOptions));
        for (final var option : enumeration.getEnumConstants ()) {
            final var check = new CheckBox (option.name ());
            check.setSelected (options.contains (option));
            check.selectedProperty ().addListener ((__, ___, selected) -> {
                if (selected) {
                    options.add (option);
                } else {
                    options.remove (option);
                }
            });
            
            getChildren ().add (check);
        }
    }
    
    public Set <E> getOptions () {
        return Set.copyOf (options);
    }
    
}
