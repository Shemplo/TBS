package ru.shemplo.tbs.entity;

import java.time.LocalDate;
import java.util.ArrayList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DateCredit extends ArrayList <Credit> {
    
    private static final long serialVersionUID = 1L;
    
    private final LocalDate date;
    
}
