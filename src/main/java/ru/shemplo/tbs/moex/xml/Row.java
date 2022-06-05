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
    
    @XmlAttribute (name = "recorddate")
    @Getter (value = AccessLevel.PRIVATE)
    private String _recorddate;
    
    @XmlAttribute
    private String boardid;
    
    @XmlAttribute (name = "is_primary")
    private boolean isPrimary;
    
    @XmlAttribute (name = "LAST")
    private String lastPrice;
    
    @XmlAttribute (name = "LCURRENTPRICE")
    private String lastCurrentPrice;
    
    @XmlAttribute (name = "MARKETPRICE")
    private String marketPrice;
    
    @XmlAttribute (name = "ACCRUEDINT")
    private String accCouponIncome;
    
    @XmlAttribute (name = "LOTVALUE")
    private String lotValue;
    
    @XmlAttribute (name = "offerdate")
    @Getter (value = AccessLevel.PRIVATE)
    private String _offerdate;
    
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
    
    public LocalDate getCouponRecordLocalDate () {
        return _recorddate == null || _recorddate.isBlank () ? null : LocalDate.parse (_recorddate);
    }
    
    public LocalDate getOfferLocalDate () {
        return _offerdate == null || _offerdate.isBlank () || "0000-00-00".equals (_offerdate) 
             ? null : LocalDate.parse (_offerdate);
    }
    
    public String getPrice () {
        return lastPrice == null || lastPrice.isBlank () 
             ? marketPrice == null || marketPrice.isBlank ()
                 ? lastCurrentPrice == null || lastCurrentPrice.isBlank () ? "" : lastCurrentPrice
                 : marketPrice
             : lastPrice;
    }
    
}
