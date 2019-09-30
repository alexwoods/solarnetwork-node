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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Network node that is a user/receiver of the assigned
 *                 signal.
 * 
 * <p>Java class for ConsumerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConsumerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NodeRef" type="{http://kayak.2codeornot2code.org/1.0}NodeRefType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConsumerType", propOrder = {
    "nodeRef"
})
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ConsumerType {

    @XmlElement(name = "NodeRef", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected List<NodeRefType> nodeRef;

    /**
     * Gets the value of the nodeRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nodeRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNodeRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NodeRefType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-10-01T10:58:48+13:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public List<NodeRefType> getNodeRef() {
        if (nodeRef == null) {
            nodeRef = new ArrayList<NodeRefType>();
        }
        return this.nodeRef;
    }

}
