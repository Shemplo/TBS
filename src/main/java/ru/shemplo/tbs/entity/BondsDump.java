package ru.shemplo.tbs.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.shemplo.tbs.TBSBondManager;
import ru.shemplo.tbs.TBSCurrencyManager;

@Getter
@RequiredArgsConstructor
public class BondsDump implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final IProfile profile;
    private final TBSCurrencyManager currencyManager;
    private final TBSBondManager bondManager;
    
}
