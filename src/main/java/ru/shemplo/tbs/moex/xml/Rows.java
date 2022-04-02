package ru.shemplo.tbs.moex.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter @Setter
@XmlAccessorType (XmlAccessType.FIELD)
public class Rows {
    
    @XmlElements (@XmlElement (name = "row"))
    private List <Row> rows;
    
    public Row getFirstRow () {
        return rows == null || rows.isEmpty () ? null : rows.get (0);
    }
    
}
