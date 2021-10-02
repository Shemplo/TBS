package ru.shemplo.tbs.moex.xml;

import java.time.LocalDate;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlAccessorType (XmlAccessType.FIELD)
public class Data {
    
    @XmlAttribute
    private String id;
    
    @XmlElement
    private Metadata metadata;
    
    @XmlElement
    private Rows rows;
    
    public Optional <String> getBondName () {
        return findRowWithName ("NAME").map (Row::getValue);
    }
    
    public Optional <String> getBondShortName () {
        return findRowWithName ("SHORTNAME").map (Row::getValue);
    }
    
    public Optional <String> getBondCode () {
        return findRowWithName ("SECID").map (Row::getValue);
    }
    
    public Optional <Integer> getBondCouponsPerYear () {
        return findRowWithName ("COUPONFREQUENCY").map (Row::getValueAsInteger);
    }
    
    public Optional <LocalDate> getBondStartDate () {
        return findRowWithName ("ISSUEDATE").map (Row::getValueAsLocalDate);
    }
    
    public Optional <LocalDate> getBondEndDate () {
        return findRowWithName ("MATDATE").map (Row::getValueAsLocalDate);
    }
    
    public Optional <LocalDate> getBondNextCouponDate () {
        return findRowWithName ("COUPONDATE").map (Row::getValueAsLocalDate);
    }
    
    public Optional <Double> getBondNominalValue () {
        return findRowWithName ("FACEVALUE").map (Row::getValueAsDouble);
    }
    
    public Optional <Double> getBondPercentage () {
        return findRowWithName ("COUPONPERCENT").map (Row::getValueAsDouble);
    }
    
    public Optional <Long> getBondDaysPerEnd () {
        return findRowWithName ("DAYSTOREDEMPTION").map (Row::getValueAsLong);
    }
    
    public Optional <Long> getBondEmitterID () {
        return findRowWithName ("EMITTER_ID").map (Row::getValueAsLong);
    }
    
    private Optional <Row> findRowWithName (String name) {
        if (rows == null || rows.getRows () == null) { return Optional.empty (); }
        
        return rows.getRows ().stream ().filter (r -> name.equals (r.getName ()))
             . findFirst ();
    }
    
    public Optional <Row> findPrimaryRow () {
        if (rows == null || rows.getRows () == null) { return Optional.empty (); }
        
        return rows.getRows ().stream ().filter (Row::isPrimary).findFirst ();
    }
    
}
