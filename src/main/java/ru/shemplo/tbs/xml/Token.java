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
public class Token {
    
    @XmlAttribute
    private String filename;
    
    @XmlAttribute
    private boolean responsible = true;
    
}
