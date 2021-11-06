package ru.shemplo.tbs;

public interface Throwing3Function <I1, I2, I3, O, E extends Exception> {
    
    O apply (I1 input1, I2 input2, I3 input) throws E;
    
}
