package ru.shemplo.tbs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BondCreditRating {
    
    HIGH        (0.0), 
    MEDIUM      (0.1), 
    LOW         (0.5), 
    SPECULATIVE (0.75), 
    UNDEFINED   (0.05)
    
    ;
    
    private final double penalty;
    
}
