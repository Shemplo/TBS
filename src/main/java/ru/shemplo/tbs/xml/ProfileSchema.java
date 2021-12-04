package ru.shemplo.tbs.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlRootElement (name = "profile")
@XmlAccessorType (XmlAccessType.FIELD)
public class ProfileSchema {
    
    @XmlElement
    private String name;
    
    @XmlElement
    private Token token;
    
    @XmlElement
    private General general;
    
    @XmlElement
    private Params params;
    
    @XmlElement
    private String currencies = "";
    
    @XmlElement
    private String cmodes = "";
    
    @XmlElement (name = "bannede")
    private String bannedEmitters = "";
    
}
