//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.10.07 at 02:37:07 PM NZDT 
//


package net.solarnetwork.node.io.canbus.kcd;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * A discrete part of information contained in the payload of a
 *                 message.
 * 
 * <p>Java class for SignalType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SignalType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://kayak.2codeornot2code.org/1.0}BasicSignalType">
 *       &lt;sequence>
 *         &lt;element name="Notes" type="{http://kayak.2codeornot2code.org/1.0}NotesType" minOccurs="0"/>
 *         &lt;element name="Consumer" type="{http://kayak.2codeornot2code.org/1.0}ConsumerType" minOccurs="0"/>
 *         &lt;element name="Value" type="{http://kayak.2codeornot2code.org/1.0}ValueType" minOccurs="0"/>
 *         &lt;element name="LabelSet" type="{http://kayak.2codeornot2code.org/1.0}LabelSetType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "notes",
    "consumer",
    "value",
    "labelSet"
})
@XmlSeeAlso({
    SignalType.class
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class OriginalSignalType
    extends BasicSignalType
{

    @XmlElement(name = "Notes")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String notes;
    @XmlElement(name = "Consumer")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ConsumerType consumer;
    @XmlElement(name = "Value")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected ValueType value;
    @XmlElement(name = "LabelSet")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected LabelSetType labelSet;

    /**
     * Gets the value of the notes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * Gets the value of the consumer property.
     * 
     * @return
     *     possible object is
     *     {@link ConsumerType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ConsumerType getConsumer() {
        return consumer;
    }

    /**
     * Sets the value of the consumer property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConsumerType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setConsumer(ConsumerType value) {
        this.consumer = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link ValueType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public ValueType getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setValue(ValueType value) {
        this.value = value;
    }

    /**
     * Gets the value of the labelSet property.
     * 
     * @return
     *     possible object is
     *     {@link LabelSetType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public LabelSetType getLabelSet() {
        return labelSet;
    }

    /**
     * Sets the value of the labelSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link LabelSetType }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setLabelSet(LabelSetType value) {
        this.labelSet = value;
    }

}
