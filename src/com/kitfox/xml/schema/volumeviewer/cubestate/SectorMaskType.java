//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.10.17 um 06:15:01 PM CEST 
//


package com.kitfox.xml.schema.volumeviewer.cubestate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für sectorMaskType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="sectorMaskType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mask" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="center" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}vectorType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sectorMaskType", propOrder = {
    "mask",
    "center"
})
public class SectorMaskType {

    protected int mask;
    @XmlElement(required = true)
    protected VectorType center;

    /**
     * Ruft den Wert der mask-Eigenschaft ab.
     * 
     */
    public int getMask() {
        return mask;
    }

    /**
     * Legt den Wert der mask-Eigenschaft fest.
     * 
     */
    public void setMask(int value) {
        this.mask = value;
    }

    /**
     * Ruft den Wert der center-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VectorType }
     *     
     */
    public VectorType getCenter() {
        return center;
    }

    /**
     * Legt den Wert der center-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VectorType }
     *     
     */
    public void setCenter(VectorType value) {
        this.center = value;
    }

}
