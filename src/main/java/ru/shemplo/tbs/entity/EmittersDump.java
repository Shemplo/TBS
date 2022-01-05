package ru.shemplo.tbs.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.tbs.TBSEmitterManager;

@Getter
@RequiredArgsConstructor
public class EmittersDump implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final TBSEmitterManager emitterManager;
    
}
