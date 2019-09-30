//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.10.01 at 10:58:48 AM NZDT 
//


package net.solarnetwork.node.io.canbus.kcd;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A group of signals that is just valid when the count value of the
 *                 group matches with the looping counter (Multiplex).
 * 
 * <p>Java class for MuxGroupType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MuxGroupType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Signal" type="{http://kayak.2codeornot2code.org/1.0}SignalType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="count" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long">
 *             &lt;minInclusive value="0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MuxGroupType", propOrder = {
    "signal"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class MuxGroupType {

    @XmlElement(name = "Signal", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<SignalType> signal;
    @XmlAttribute(name = "count", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected long count;

    /**
     * Gets the value of the signal property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the signal property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSignal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SignalType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<SignalType> getSignal() {
        if (signal == null) {
            signal = new ArrayList<SignalType>();
        }
        return this.signal;
    }

    /**
     * Gets the value of the count property.
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setCount(long value) {
        this.count = value;
    }

}
