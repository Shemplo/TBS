package ru.shemplo.tbs.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlAccessorType (XmlAccessType.FIELD)
public class Params {
    
    @XmlAttribute
    private Long mte;
    
    @XmlAttribute
    private Long cpy;
    
    @XmlAttribute
    private Long mdtc;
    
    @XmlAttribute
    private Double nv;
    
    @XmlAttribute
    private Double minp;
    
    @XmlAttribute
    private Double maxpr;
    
}
