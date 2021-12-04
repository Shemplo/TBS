package ru.shemplo.tbs.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LauncherDump implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final List <IProfile> profiles;
    
}
