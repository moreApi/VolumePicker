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
 * <p>Java-Klasse für transferType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="transferType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="transferFunction" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="brushSize" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="brushOpacity" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="brushSharp" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="brushColor" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}vectorType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "transferType", propOrder = {
    "transferFunction",
    "brushSize",
    "brushOpacity",
    "brushSharp",
    "brushColor"
})
public class TransferType {

    @XmlElement(required = true)
    protected byte[] transferFunction;
    @XmlElement(defaultValue = "10")
    protected float brushSize;
    @XmlElement(defaultValue = ".5")
    protected float brushOpacity;
    @XmlElement(defaultValue = ".5")
    protected float brushSharp;
    @XmlElement(required = true)
    protected VectorType brushColor;

    /**
     * Ruft den Wert der transferFunction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getTransferFunction() {
        return transferFunction;
    }

    /**
     * Legt den Wert der transferFunction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setTransferFunction(byte[] value) {
        this.transferFunction = value;
    }

    /**
     * Ruft den Wert der brushSize-Eigenschaft ab.
     * 
     */
    public float getBrushSize() {
        return brushSize;
    }

    /**
     * Legt den Wert der brushSize-Eigenschaft fest.
     * 
     */
    public void setBrushSize(float value) {
        this.brushSize = value;
    }

    /**
     * Ruft den Wert der brushOpacity-Eigenschaft ab.
     * 
     */
    public float getBrushOpacity() {
        return brushOpacity;
    }

    /**
     * Legt den Wert der brushOpacity-Eigenschaft fest.
     * 
     */
    public void setBrushOpacity(float value) {
        this.brushOpacity = value;
    }

    /**
     * Ruft den Wert der brushSharp-Eigenschaft ab.
     * 
     */
    public float getBrushSharp() {
        return brushSharp;
    }

    /**
     * Legt den Wert der brushSharp-Eigenschaft fest.
     * 
     */
    public void setBrushSharp(float value) {
        this.brushSharp = value;
    }

    /**
     * Ruft den Wert der brushColor-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VectorType }
     *     
     */
    public VectorType getBrushColor() {
        return brushColor;
    }

    /**
     * Legt den Wert der brushColor-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VectorType }
     *     
     */
    public void setBrushColor(VectorType value) {
        this.brushColor = value;
    }

}
