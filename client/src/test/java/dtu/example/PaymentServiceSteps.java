package dtu.example;

import dtu.example.record.Customer;
import dtu.example.record.Merchant;
import dtu.example.service.SimpleDtuPay;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertTrue;

public class PaymentServiceSteps {
    private Customer customer;
    private Merchant merchant;
    private String customerId, merchantId;
    private SimpleDtuPay dtupay = new SimpleDtuPay();
    private boolean successful = false;
    @Given("a customer with name {string}")
    public void aCustomerWithName(String name) {
        customer = new Customer(name);
    }
    @Given("the customer is registered with Simple DTU Pay")
    public void theCustomerIsRegisteredWithSimpleDTUPay() {
        customerId = dtupay.register(customer);
    }
    @Given("a merchant with name {string}")
    public void aMerchantWithName(String name) {
        merchant = new Merchant(name);
    }
    @Given("the merchant is registered with Simple DTU Pay")
    public void theMerchantIsRegisteredWithSimpleDTUPay() {
        merchantId = dtupay.register(merchant);
    }
    @When("the merchant initiates a payment for {int} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(Integer amount) {
        successful = dtupay.pay(amount,customerId,merchantId);
    }
    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertTrue(successful);
    }
}