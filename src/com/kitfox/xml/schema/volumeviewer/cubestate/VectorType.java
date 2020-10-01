//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.10.17 um 06:15:01 PM CEST 
//


package com.kitfox.xml.schema.volumeviewer.cubestate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für vectorType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="vectorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}float" default="0" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}float" default="0" />
 *       &lt;attribute name="z" type="{http://www.w3.org/2001/XMLSchema}float" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vectorType")
public class VectorType {

    @XmlAttribute(name = "x")
    protected Float x;
    @XmlAttribute(name = "y")
    protected Float y;
    @XmlAttribute(name = "z")
    protected Float z;

    /**
     * Ruft den Wert der x-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public float getX() {
        if (x == null) {
            return  0.0F;
        } else {
            return x;
        }
    }

    /**
     * Legt den Wert der x-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setX(Float value) {
        this.x = value;
    }

    /**
     * Ruft den Wert der y-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public float getY() {
        if (y == null) {
            return  0.0F;
        } else {
            return y;
        }
    }

    /**
     * Legt den Wert der y-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setY(Float value) {
        this.y = value;
    }

    /**
     * Ruft den Wert der z-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public float getZ() {
        if (z == null) {
            return  0.0F;
        } else {
            return z;
        }
    }

    /**
     * Legt den Wert der z-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setZ(Float value) {
        this.z = value;
    }

}
