package ru.shemplo.tbs.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.tbs.TBSPlanner;

@Getter
@RequiredArgsConstructor
public class PlanningDump implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final TBSPlanner planner;
    
}
