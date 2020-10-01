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
 * <p>Java-Klasse für cubeType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="cubeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="volumeRadius" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}vectorType"/>
 *         &lt;element name="lightDir" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}vectorType"/>
 *         &lt;element name="lightColor" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}vectorType"/>
 *         &lt;element name="lightingStyle" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}lightingStyleType"/>
 *         &lt;element name="sectorMask" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}sectorMaskType"/>
 *         &lt;element name="opacityRef" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="numPlanes" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="drawWireframe" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="drawLightbuffer" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="transfer" type="{http://xml.kitfox.com/schema/volumeViewer/cubeState}transferType"/>
 *         &lt;element name="multisampled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cubeType", propOrder = {
    "volumeRadius",
    "lightDir",
    "lightColor",
    "lightingStyle",
    "sectorMask",
    "opacityRef",
    "numPlanes",
    "drawWireframe",
    "drawLightbuffer",
    "transfer",
    "multisampled"
})
public class CubeType {

    @XmlElement(required = true)
    protected VectorType volumeRadius;
    @XmlElement(required = true)
    protected VectorType lightDir;
    @XmlElement(required = true)
    protected VectorType lightColor;
    @XmlElement(required = true, defaultValue = "NONE")
    protected LightingStyleType lightingStyle;
    @XmlElement(required = true)
    protected SectorMaskType sectorMask;
    protected float opacityRef;
    @XmlElement(defaultValue = "1")
    protected int numPlanes;
    @XmlElement(defaultValue = "true")
    protected boolean drawWireframe;
    @XmlElement(defaultValue = "true")
    protected boolean drawLightbuffer;
    @XmlElement(required = true)
    protected TransferType transfer;
    @XmlElement(defaultValue = "false")
    protected boolean multisampled;

    /**
     * Ruft den Wert der volumeRadius-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VectorType }
     *     
     */
    public VectorType getVolumeRadius() {
        return volumeRadius;
    }

    /**
     * Legt den Wert der volumeRadius-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VectorType }
     *     
     */
    public void setVolumeRadius(VectorType value) {
        this.volumeRadius = value;
    }

    /**
     * Ruft den Wert der lightDir-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VectorType }
     *     
     */
    public VectorType getLightDir() {
        return lightDir;
    }

    /**
     * Legt den Wert der lightDir-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VectorType }
     *     
     */
    public void setLightDir(VectorType value) {
        this.lightDir = value;
    }

    /**
     * Ruft den Wert der lightColor-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VectorType }
     *     
     */
    public VectorType getLightColor() {
        return lightColor;
    }

    /**
     * Legt den Wert der lightColor-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VectorType }
     *     
     */
    public void setLightColor(VectorType value) {
        this.lightColor = value;
    }

    /**
     * Ruft den Wert der lightingStyle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LightingStyleType }
     *     
     */
    public LightingStyleType getLightingStyle() {
        return lightingStyle;
    }

    /**
     * Legt den Wert der lightingStyle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LightingStyleType }
     *     
     */
    public void setLightingStyle(LightingStyleType value) {
        this.lightingStyle = value;
    }

    /**
     * Ruft den Wert der sectorMask-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SectorMaskType }
     *     
     */
    public SectorMaskType getSectorMask() {
        return sectorMask;
    }

    /**
     * Legt den Wert der sectorMask-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SectorMaskType }
     *     
     */
    public void setSectorMask(SectorMaskType value) {
        this.sectorMask = value;
    }

    /**
     * Ruft den Wert der opacityRef-Eigenschaft ab.
     * 
     */
    public float getOpacityRef() {
        return opacityRef;
    }

    /**
     * Legt den Wert der opacityRef-Eigenschaft fest.
     * 
     */
    public void setOpacityRef(float value) {
        this.opacityRef = value;
    }

    /**
     * Ruft den Wert der numPlanes-Eigenschaft ab.
     * 
     */
    public int getNumPlanes() {
        return numPlanes;
    }

    /**
     * Legt den Wert der numPlanes-Eigenschaft fest.
     * 
     */
    public void setNumPlanes(int value) {
        this.numPlanes = value;
    }

    /**
     * Ruft den Wert der drawWireframe-Eigenschaft ab.
     * 
     */
    public boolean isDrawWireframe() {
        return drawWireframe;
    }

    /**
     * Legt den Wert der drawWireframe-Eigenschaft fest.
     * 
     */
    public void setDrawWireframe(boolean value) {
        this.drawWireframe = value;
    }

    /**
     * Ruft den Wert der drawLightbuffer-Eigenschaft ab.
     * 
     */
    public boolean isDrawLightbuffer() {
        return drawLightbuffer;
    }

    /**
     * Legt den Wert der drawLightbuffer-Eigenschaft fest.
     * 
     */
    public void setDrawLightbuffer(boolean value) {
        this.drawLightbuffer = value;
    }

    /**
     * Ruft den Wert der transfer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransferType }
     *     
     */
    public TransferType getTransfer() {
        return transfer;
    }

    /**
     * Legt den Wert der transfer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransferType }
     *     
     */
    public void setTransfer(TransferType value) {
        this.transfer = value;
    }

    /**
     * Ruft den Wert der multisampled-Eigenschaft ab.
     * 
     */
    public boolean isMultisampled() {
        return multisampled;
    }

    /**
     * Legt den Wert der multisampled-Eigenschaft fest.
     * 
     */
    public void setMultisampled(boolean value) {
        this.multisampled = value;
    }

}
