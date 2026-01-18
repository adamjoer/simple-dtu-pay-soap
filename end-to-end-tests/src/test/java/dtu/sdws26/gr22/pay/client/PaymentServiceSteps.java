package dtu.sdws26.gr22.pay.client;

import dtu.sdws26.gr22.pay.client.record.Customer;
import dtu.sdws26.gr22.pay.client.record.Merchant;
import dtu.sdws26.gr22.pay.client.record.Payment;
import dtu.sdws26.gr22.pay.client.service.CustomerService;
import dtu.sdws26.gr22.pay.client.service.MerchantService;
import dtu.sdws26.gr22.pay.client.service.PaymentService;
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

public class PaymentServiceSteps {
    private final String API_KEY = System.getenv("SIMPLE_DTU_PAY_API_KEY");

    private Customer customer;
    private Merchant merchant;


    private final CustomerService customerService = new CustomerService();
    private final MerchantService merchantService = new MerchantService();
    private final PaymentService paymentService = new PaymentService();

    private boolean successful = false;
    private String errorMessage;

    private final BankService bank = new BankService_Service().getBankServicePort();
    private final List<String> accounts = new ArrayList<>();

    public String registerAccount(String firstName, String lastName, String cprNumber, String initialBalance) throws BankServiceException_Exception {
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
        if (customer != null && customer.id != null) {
            customerService.unregister(customer);
        }
        if (merchant != null && merchant.id != null) {
            merchantService.unregister(merchant);
        }

        for (var account : accounts) {
            bank.retireAccount(API_KEY, account);
        }
    }

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String firstName, String lastName, String cprNumber) {
        customer = new Customer(null, firstName, lastName, cprNumber, null);
    }

    @Given("the customer is registered with the bank with an initial balance of {string} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String balance) throws BankServiceException_Exception {
        var bankId = registerAccount(customer.firstName, customer.lastName, customer.cprNumber, balance);
        customer.bankId = bankId;
    }

    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() throws BankServiceException_Exception {
        customer = customerService.register(customer);
        Assert.assertNotNull(customer.id);
    }

    @Given("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String firstName, String lastName, String cprNumber) {
        merchant = new Merchant(null, firstName, lastName, cprNumber, null);
    }

    @Given("the merchant is registered with the bank with an initial balance of {string} kr")
    public void theMerchantIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String amount) throws Exception {
        var bankId = registerAccount(merchant.firstName, merchant.lastName, merchant.cprNumber, amount);
        merchant.bankId = bankId;
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        merchant = merchantService.register(merchant);
        Assert.assertNotNull(merchant.id);
    }

    @When("the merchant initiates a payment for {string} kr by the customer")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(String amount) {
        try {
            successful = paymentService.pay(amount, customer.id, merchant.id);
        } catch (Exception e) {
            successful = false;
            errorMessage = e.getMessage();
        }
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        Assert.assertTrue("Expected payment to be successful, but it failed with error: " + errorMessage, successful);
    }

    @Then("the balance of the customer at the bank is {string} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(String amount) throws Exception {
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        BigDecimal balance = bank.getAccount(customer.bankId).getBalance();
        Assert.assertEquals(
                "Expected balance " + amountBigDecimal + " but was " + balance,
                0, amountBigDecimal.compareTo(balance)
        );
    }

    @Then("the balance of the merchant at the bank is {string} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(String amount) throws Exception {
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        BigDecimal balance = bank.getAccount(merchant.bankId).getBalance();
        Assert.assertEquals(
                "Expected balance " + amountBigDecimal + " but was " + balance,
                0, amountBigDecimal.compareTo(balance)
        );
    }
}
