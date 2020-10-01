//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.10.17 um 06:15:01 PM CEST 
//


package com.kitfox.xml.schema.volumeviewer.cubestate;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für lightingStyleType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="lightingStyleType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NONE"/>
 *     &lt;enumeration value="PHONG"/>
 *     &lt;enumeration value="DIFFUSE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "lightingStyleType")
@XmlEnum
public enum LightingStyleType {

    NONE,
    PHONG,
    DIFFUSE;

    public String value() {
        return name();
    }

    public static LightingStyleType fromValue(String v) {
        return valueOf(v);
    }

}
