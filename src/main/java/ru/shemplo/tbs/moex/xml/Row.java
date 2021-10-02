package ru.shemplo.tbs.moex.xml;

import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlAccessorType (XmlAccessType.FIELD)
public class Row {
    
    @XmlAttribute
    private String name;
    
    @XmlAttribute
    private String secid;
    
    @XmlAttribute
    private String title;
    
    @XmlAttribute
    private String value;
    
    @XmlAttribute
    private String type;
    
    @XmlAttribute
    private int sortOrder;
    
    @XmlAttribute (name = "is_hidden")
    private boolean isHidden;
    
    @XmlAttribute (name = "precision")
    @Getter (value = AccessLevel.PRIVATE)
    private String _precision;
    
    @XmlAttribute (name = "coupondate")
    @Getter (value = AccessLevel.PRIVATE)
    private String _coupondate;
    
    @XmlAttribute
    private String boardid;
    
    @XmlAttribute (name = "is_primary")
    private boolean isPrimary;
    
    @XmlAttribute (name = "LAST")
    private String lastPrice;
    
    public LocalDate getValueAsLocalDate () {
        return value == null || value.isBlank () ? null : LocalDate.parse (value);
    }
    
    public Integer getValueAsInteger () {
        return value == null || value.isBlank () ? null : Integer.parseInt (value);
    }
    
    public Long getValueAsLong () {
        return value == null || value.isBlank () ? null : Long.parseLong (value);
    }
    
    public Double getValueAsDouble () {
        return value == null || value.isBlank () ? null : Double.parseDouble (value);
    }
    
    public LocalDate getCouponLocalDate () {
        return _coupondate == null || _coupondate.isBlank () ? null : LocalDate.parse (_coupondate);
    }
    
}
