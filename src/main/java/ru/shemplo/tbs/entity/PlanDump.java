package ru.shemplo.tbs.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.tbs.TBSPlanner.DistributionCategory;

@Getter
@RequiredArgsConstructor
public class PlanDump implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final List <PlanningBond> bonds; 
    
    private final DistributionCategory category;
    
    private final double amount, diversification;
    
}
