//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.10.07 at 07:38:56 AM NZDT 
//


package net.solarnetwork.node.io.canbus.kcd;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Definition of one or more CAN bus networks in one
 *                 file.
 * 
 * <p>Java class for NetworkDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NetworkDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Document" type="{http://kayak.2codeornot2code.org/1.0}DocumentType"/>
 *         &lt;element name="Node" type="{http://kayak.2codeornot2code.org/1.0}NodeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Bus" type="{http://kayak.2codeornot2code.org/1.0}BusType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkDefinitionType", propOrder = {
    "document",
    "node",
    "bus"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class NetworkDefinitionType {

    @XmlElement(name = "Document", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected DocumentType document;
    @XmlElement(name = "Node")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<NodeType> node;
    @XmlElement(name = "Bus", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<BusType> bus;

    /**
     * Gets the value of the document property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public DocumentType getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setDocument(DocumentType value) {
        this.document = value;
    }

    /**
     * Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NodeType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<NodeType> getNode() {
        if (node == null) {
            node = new ArrayList<NodeType>();
        }
        return this.node;
    }

    /**
     * Gets the value of the bus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BusType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T07:38:56+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<BusType> getBus() {
        if (bus == null) {
            bus = new ArrayList<BusType>();
        }
        return this.bus;
    }

}
