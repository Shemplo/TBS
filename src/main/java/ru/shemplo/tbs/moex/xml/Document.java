package ru.shemplo.tbs.moex.xml;

import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlRootElement (name = "document")
@XmlAccessorType (XmlAccessType.FIELD)
public class Document {
    
    @XmlElements (@XmlElement (name = "data"))
    private List <Data> data;
    
    public Optional <Data> getDescription () {
        return findData ("description");
    }
    
    public Optional <Data> getBoards () {
        return findData ("boards");
    }
    
    public Optional <Data> getCoupons () {
        return findData ("coupons");
    }
    
    public Optional <Data> getOffers () {
        return findData ("offers");
    }
    
    public Optional <Data> getMarketData () {
        return findData ("marketdata");
    }
    
    public Optional <Data> getSecuritiesData () {
        return findData ("securities");
    }
    
    private Optional <Data> findData (String name) {
        return data.stream ().filter (data -> name.equals (data.getId ())).findFirst ();
    }
    
}
