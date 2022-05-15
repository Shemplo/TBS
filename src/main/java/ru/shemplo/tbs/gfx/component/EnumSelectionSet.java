package ru.shemplo.tbs.gfx.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.FlowPane;

public class EnumSelectionSet <E extends Enum <E>> extends FlowPane {
    
    private final ObservableSet <E> options;
    private final Class <E> enumeration;
    
    public EnumSelectionSet (Class <E> enumeration) {
        this (enumeration, List.of ());
    }
    
    public EnumSelectionSet (Class <E> enumeration, Collection <E> selectedOptions) {
        this.enumeration = enumeration;
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
    
    public static <E extends Enum <E>> EnumSelectionSet <E> noneOf (Class <E> enumeration) {
        return new EnumSelectionSet <> (enumeration);
    }
    
    public static <E extends Enum <E>> EnumSelectionSet <E> allOf (Class <E> enumeration) {
        return new EnumSelectionSet <> (enumeration, Arrays.asList (enumeration.getEnumConstants ()));
    }
    
    public Set <E> getOptions () {
        return Set.copyOf (options);
    }
    
    public ObservableSet <E> getObservableOptions () {
        return options;
    }

    public void setOptionsNameConverter (Function <E, String> converter) {
        final var options = enumeration.getEnumConstants ();
        final var checkBoxes = getChildren ();
        
        for (int i = 0; i < checkBoxes.size (); i++) {
            final var check = (CheckBox) checkBoxes.get (i);
            check.setText (converter.apply (options [i]));
        }
    }
    
}
