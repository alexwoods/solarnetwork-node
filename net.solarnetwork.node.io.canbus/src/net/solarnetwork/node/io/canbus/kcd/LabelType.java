//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.10.07 at 02:37:07 PM NZDT 
//


package net.solarnetwork.node.io.canbus.kcd;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Descriptive name for a single value e.g. to describe an enumeration,
 *                 mark special,invalid or error values.
 * 
 * <p>Java class for LabelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LabelType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://kayak.2codeornot2code.org/1.0}BasicLabelType">
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LabelType")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class LabelType
    extends BasicLabelType
{

    @XmlAttribute(name = "value")
    @XmlSchemaType(name = "nonNegativeInteger")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger value;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-07T02:37:07+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setValue(BigInteger value) {
        this.value = value;
    }

}
