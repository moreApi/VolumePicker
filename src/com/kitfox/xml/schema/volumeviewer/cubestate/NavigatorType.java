//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.10.17 um 06:15:01 PM CEST 
//


package com.kitfox.xml.schema.volumeviewer.cubestate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für navigatorType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="navigatorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="yaw" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="pitch" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="radius" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "navigatorType", propOrder = {
    "yaw",
    "pitch",
    "radius"
})
public class NavigatorType {

    protected float yaw;
    protected float pitch;
    protected float radius;

    /**
     * Ruft den Wert der yaw-Eigenschaft ab.
     * 
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Legt den Wert der yaw-Eigenschaft fest.
     * 
     */
    public void setYaw(float value) {
        this.yaw = value;
    }

    /**
     * Ruft den Wert der pitch-Eigenschaft ab.
     * 
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Legt den Wert der pitch-Eigenschaft fest.
     * 
     */
    public void setPitch(float value) {
        this.pitch = value;
    }

    /**
     * Ruft den Wert der radius-Eigenschaft ab.
     * 
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Legt den Wert der radius-Eigenschaft fest.
     * 
     */
    public void setRadius(float value) {
        this.radius = value;
    }

}
