package ru.shemplo.tbs.entity;

import java.io.Serializable;

public interface IEmitter extends Serializable, ObservableEntity <IEmitter> {
    
    long getId ();
    
    String getName ();
    
    BondCreditRating getRating ();
    
}
