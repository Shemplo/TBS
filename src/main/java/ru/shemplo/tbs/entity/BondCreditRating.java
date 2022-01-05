package ru.shemplo.tbs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BondCreditRating {
    
    HIGH        (0.0), 
    MEDIUM      (0.1), 
    LOW         (0.3), 
    SPECULATIVE (0.5), 
    UNDEFINED   (0.0)
    
    ;
    
    private final double penalty;
    
}
