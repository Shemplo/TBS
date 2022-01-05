package ru.shemplo.tbs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@RequiredArgsConstructor
public class Emitter extends AbstractObservableEntity <IEmitter> implements IEmitter {
    
    private static final long serialVersionUID = 1L;
    
    private final long id;
    
    private String name;
    
    private BondCreditRating rating;
    
}
