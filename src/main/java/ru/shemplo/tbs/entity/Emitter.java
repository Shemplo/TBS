package ru.shemplo.tbs.entity;

import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@RequiredArgsConstructor
public class Emitter extends AbstractObservableEntity <IEmitter> implements IEmitter, UpdateDateTracker {
    
    private static final long serialVersionUID = 1L;
    
    private final long id;
    
    private String name;
    
    private BondCreditRating rating = BondCreditRating.UNDEFINED;
    
    private Date updated;
    
}
