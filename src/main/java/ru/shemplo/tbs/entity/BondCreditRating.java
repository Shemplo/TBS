package ru.shemplo.tbs.entity;

import static ru.shemplo.tbs.gfx.TBSApplicationIcons.*;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BondCreditRating {
    
    HIGH        (0.0, approved24), 
    MEDIUM      (0.1, warning24), 
    LOW         (0.5, error24), 
    SPECULATIVE (0.75, fatal24), 
    UNDEFINED   (0.05, question)
    
    ;
    
    private final double penalty;
    
    private final Image icon;
    
}
