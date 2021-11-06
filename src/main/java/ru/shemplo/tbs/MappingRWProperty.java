package ru.shemplo.tbs;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor (access = AccessLevel.PRIVATE)
public class MappingRWProperty <F, S> extends SimpleObjectProperty <S> {
    
    public static <F, S> MappingRWProperty <F, S> of (
        Property <F> dependency, 
        Throwing3Function <F, S, Boolean, S, RuntimeException> mapperFS, 
        Throwing3Function <S, F, Boolean, F, RuntimeException> mapperSF
    ) {
        final var rw = new MappingRWProperty <F, S> ();
        TBSUtils.bindBidirectionalMapping (dependency, rw, mapperFS, mapperSF);
        rw.set (mapperFS.apply (dependency.getValue (), null, true));
        return rw;
    }
    
}
