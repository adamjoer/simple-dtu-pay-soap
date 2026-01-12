package dtu.example;

import dtu.example.record.Customer;
import dtu.example.record.Merchant;
import dtu.example.record.Payment;
import dtu.example.service.DTUPayService;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PaymentServiceSteps {
    private final String API_KEY = System.getenv("SIMPLE_DTU_PAY_API_KEY");

    private Customer customer;
    private Merchant merchant;
    private String customerId, merchantId;
    private final DTUPayService payService = new DTUPayService();

    private Collection<Payment> payments;

    private boolean successful = false;
    private String errorMessage;

    private final BankService bank = new BankService_Service().getBankServicePort();
    private final List<String> accounts = new ArrayList<>();

    public String registerAccount(String firstName, String lastName, String cprNumber, double initialBalance) throws BankServiceException_Exception {
        Assert.assertNotNull("API_KEY environment variable is not set", API_KEY);
        var user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCprNumber(cprNumber);

        var balance = new BigDecimal(initialBalance);
        var account = bank.createAccountWithBalance(API_KEY, user, balance);
        accounts.add(account);
        return account;
    }

    @After
    public void tearDown() throws BankServiceException_Exception {
        if (customerId != null) {
            payService.unregisterCustomer(customerId);
        }
        if (merchantId != null) {
            payService.unregisterMerchant(merchantId);
        }

        for (var account : accounts) {
            bank.retireAccount(API_KEY, account);
        }
    }

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String firstName, String lastName, String cprNumber) {
        customer = new Customer(null, firstName, lastName, cprNumber, null);
    }

    @Given("the customer is registered with the bank with an initial balance of {double} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(Double balance) throws BankServiceException_Exception {
        var bankId = registerAccount(customer.firstName, customer.lastName, customer.cprNumber, balance);
        customer.bankId = bankId;
    }

    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() throws BankServiceException_Exception {
        customerId = payService.register(customer);
    }

    @Given("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String firstName, String lastName, String cprNumber) {
        // Write code here that turns the phrase above into concrete actions
        merchant = new Merchant(null, firstName, lastName, cprNumber, null);
    }

    @Given("the merchant is registered with the bank with an initial balance of {double} kr")
    public void theMerchantIsRegisteredWithTheBankWithAnInitialBalanceOfKr(Double amount) throws Exception {
        // Write code here that turns the phrase above into concrete actions
        var bankId = registerAccount(merchant.firstName, merchant.lastName, merchant.cprNumber, amount);
        merchant.bankId = bankId;
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        merchantId = payService.register(merchant);
    }

    @When("the merchant initiates a payment for {double} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(Double amount) {
        try {
            successful = payService.pay(amount, customerId, merchantId);
        } catch (Exception e) {
            successful = false;
            errorMessage = e.getMessage();
        }
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        Assert.assertTrue("Expected payment to be successful, but it failed with error: " + errorMessage, successful);
    }

    @Then("the balance of the customer at the bank is {double} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(Double amount) throws Exception {
        BigDecimal amountBigDecimal = BigDecimal.valueOf(amount);
        BigDecimal balance = bank.getAccount(customer.bankId).getBalance();
        Assert.assertEquals(
                "Expected balance " + amountBigDecimal + " but was " + balance,
                0, amountBigDecimal.compareTo(balance)
        );
    }

    @Then("the balance of the merchant at the bank is {double} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(Double amount) throws Exception {
        BigDecimal amountBigDecimal = BigDecimal.valueOf(amount);
        BigDecimal balance = bank.getAccount(merchant.bankId).getBalance();
        Assert.assertEquals(
                "Expected balance " + amountBigDecimal + " but was " + balance,
                0, amountBigDecimal.compareTo(balance)
        );
    }
}
