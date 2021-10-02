package ru.shemplo.tbs.moex.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlAccessorType (XmlAccessType.FIELD)
public class Column {
    
    @XmlAttribute
    private String name;
    
    @XmlAttribute
    private String type;
    
    @XmlAttribute
    private int bytes;
    
    @XmlAttribute
    private int maxSize;
    
}
