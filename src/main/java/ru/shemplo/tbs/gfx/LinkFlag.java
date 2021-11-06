package ru.shemplo.tbs.gfx;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class LinkFlag {
    
    private final boolean flag;
    private final String link;
    
}
