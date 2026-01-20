
package dtu.ws.fastmoney;

import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createAccountWithBalance complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="createAccountWithBalance">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="bank-api-key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="user" type="{http://fastmoney.ws.dtu/}user" minOccurs="0"/>
 *         <element name="balance" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createAccountWithBalance", propOrder = {
    "bankApiKey",
    "user",
    "balance"
})
public class CreateAccountWithBalance {

    @XmlElement(name = "bank-api-key")
    protected String bankApiKey;
    protected User user;
    protected BigDecimal balance;

    /**
     * Gets the value of the bankApiKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBankApiKey() {
        return bankApiKey;
    }

    /**
     * Sets the value of the bankApiKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBankApiKey(String value) {
        this.bankApiKey = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link User }
     *     
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link User }
     *     
     */
    public void setUser(User value) {
        this.user = value;
    }

    /**
     * Gets the value of the balance property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Sets the value of the balance property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBalance(BigDecimal value) {
        this.balance = value;
    }

}
