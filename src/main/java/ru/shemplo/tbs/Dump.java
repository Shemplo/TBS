package ru.shemplo.tbs;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Dump implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final ITBSProfile profile;
    private final List <Bond> bonds;
    private final List <Bond> portfolio;
    
}
