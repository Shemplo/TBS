package ru.shemplo.tbs.gfx.component;

import com.panemu.tiwulfx.control.NumberField;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;

@Getter
public class SliderWithField <N extends Number> extends HBox {
    
    private final DoubleProperty valueProperty;
    private final NumberField <Number> field;
    private final Slider slider;
    
    @SuppressWarnings ("unchecked")
    public SliderWithField (Class <N> type, double min, double max, double value) {
        setSpacing (8.0);
        
        final var snap = !(type == Float.class || type == Double.class);
        getChildren ().add (slider = new Slider (min, max, value));
        HBox.setHgrow (slider, Priority.ALWAYS);
        slider.setShowTickLabels (true);
        slider.setShowTickMarks (true);
        slider.setSnapToTicks (snap);
        
        getChildren ().add (field = new NumberField <> ((Class <Number>) type));
        field.setMaxWidth (80.0);
        
        valueProperty = slider.valueProperty ();
        valueProperty.bindBidirectional (field.valueProperty ());
        valueProperty.setValue (value);
    }
    
}
