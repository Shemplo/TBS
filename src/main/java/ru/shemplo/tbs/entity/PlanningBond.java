package ru.shemplo.tbs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.shemplo.tbs.TBSUtils;

@Getter @Setter
@RequiredArgsConstructor
public class PlanningBond extends AbstractObservableEntity <IPlanningBond> implements IPlanningBond {

    private static final long serialVersionUID = 1L;
    
    private final String code;
    
    private int amount;
    
    private Integer customValue;
    
    private double recommendedPrice;
    
    @Override
    public Integer getCurrentValue () {
        return TBSUtils.aOrB (getCustomValue (), getAmount ());
    }
    
}
