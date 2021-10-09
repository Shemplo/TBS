package ru.shemplo.tbs.gfx;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter @Setter
@RequiredArgsConstructor
public class TBSMetaWrapper <T> {
    
    private final T object;
    
    private boolean hovered;
    
}
