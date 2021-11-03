package ru.shemplo.tbs.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.shemplo.tbs.TBSUtils;

@Getter @Setter
@RequiredArgsConstructor
public class PlanningBond implements Serializable, CustomValueHolder <Integer> {

    private static final long serialVersionUID = 1L;
    
    private final String code;
    
    private final double score, price;
    
    private int amount;
    private double idealAmount;
    
    private Integer customValue;
    
    @Override
    public Integer getCurrentValue () {
        return TBSUtils.aOrB (getCustomValue (), amount);
    }
    
}
