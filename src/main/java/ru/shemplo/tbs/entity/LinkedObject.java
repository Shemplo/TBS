package ru.shemplo.tbs.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class LinkedObject <T> {
    
    private final String link;
    private final T object;
    
}
