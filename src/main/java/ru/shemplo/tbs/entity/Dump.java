package ru.shemplo.tbs.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSCurrencyManager;

@Getter
@RequiredArgsConstructor
public class Dump implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final ITBSProfile profile;
    private final TBSCurrencyManager currencyManager;
    private final TBSBondManager bondManager;
    
}
