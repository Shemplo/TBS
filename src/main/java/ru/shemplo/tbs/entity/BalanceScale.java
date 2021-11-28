package ru.shemplo.tbs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BalanceScale {
    
    DAY   (0, 30, 60, 14, 93), 
    MONTH (0, 12, 24,  1, 48), 
    YEAR  (0,  5, 10,  1, 20)
    
    ;
    
    private final int defaultOffset;
    private final int maxOffset;
    private final int defaultAmount;
    private final int minAmount;
    private final int maxAmount;
    
}
