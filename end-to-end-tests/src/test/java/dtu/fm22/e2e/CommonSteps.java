package dtu.fm22.e2e;

import dtu.fm22.e2e.record.Customer;
import dtu.fm22.e2e.record.Merchant;
import dtu.fm22.e2e.service.CustomerService;
import dtu.fm22.e2e.service.MerchantService;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.User;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class CommonSteps {
    private final String API_KEY = System.getenv("SIMPLE_DTU_PAY_API_KEY");
    private final SharedState state;
    private final CustomerService customerService = new CustomerService();
    private final MerchantService merchantService = new MerchantService();

    public CommonSteps(SharedState state) {
        this.state = state;
    }

    public String registerAccount(String firstName, String lastName, String cprNumber, String initialBalance) throws BankServiceException_Exception {
        assertNotNull("API_KEY environment variable is not set", API_KEY);
        var user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCprNumber(cprNumber);

        var balance = new BigDecimal(initialBalance);
        var account = state.bank.createAccountWithBalance(API_KEY, user, balance);
        state.accounts.add(account);
        return account;
    }

    @After
    public void tearDown() {
        if (state.customer != null && state.customer.id != null) {
            customerService.unregister(state.customer);
        }
        if (state.merchant != null && state.merchant.id != null) {
            merchantService.unregister(state.merchant);
        }

        try {
            for (var account : state.accounts) {
                state.bank.retireAccount(API_KEY, account);
            }
        } catch (BankServiceException_Exception e) {
            System.err.println("Failed to retire bank accounts: " + e.getMessage());
        }
    }

    @Given("a customer with name {string}, last name {string}, and CPR {string}")
    public void aCustomerWithNameLastNameAndCPR(String firstName, String lastName, String cprNumber) {
        state.customer = new Customer(null, firstName, lastName, cprNumber, null);
    }

    @Given("the customer is registered with the bank with an initial balance of {string} kr")
    public void theCustomerIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String balance)  {
        try {
            var bankId = registerAccount(state.customer.firstName, state.customer.lastName, state.customer.cprNumber, balance);
            state.customer.bankId = bankId;
        } catch (BankServiceException_Exception e) {
            fail("Failed to register customer with bank: " + e.getMessage());
        }
    }

    @Given("the customer is registered with Simple DTU Pay using their bank account")
    public void theCustomerIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        state.customer = customerService.register(state.customer);
        assertNotNull(state.customer.id);
    }

    @Given("a merchant with name {string}, last name {string}, and CPR {string}")
    public void aMerchantWithNameLastNameAndCPR(String firstName, String lastName, String cprNumber) {
        state.merchant = new Merchant(null, firstName, lastName, cprNumber, null);
    }

    @Given("the merchant is registered with the bank with an initial balance of {string} kr")
    public void theMerchantIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String amount) {
        try {
            var bankId = registerAccount(state.merchant.firstName, state.merchant.lastName, state.merchant.cprNumber, amount);
            state.merchant.bankId = bankId;
        } catch(BankServiceException_Exception e) {
            fail("Failed to register merchant with bank: " + e.getMessage());
        }
    }

    @Given("the merchant is registered with Simple DTU Pay using their bank account")
    public void theMerchantIsRegisteredWithSimpleDTUPayUsingTheirBankAccount() {
        state.merchant = merchantService.register(state.merchant);
        assertNotNull(state.merchant.id);
    }

    @Given("the customer has {int} unused tokens")
    public void theCustomerHasUnusedTokens(Integer numberOfTokens) {
        assertNotNull("Customer must be registered before requesting tokens", state.customer);
        try {
            state.tokens = customerService.requestMoreTokens(state.customer, numberOfTokens);
        } catch (Exception e) {
            fail("Failed to request tokens: " + e.getMessage());
        }
        assertEquals("Expected to receive " + numberOfTokens + " tokens, but got " + state.tokens.size(),
                numberOfTokens.intValue(), state.tokens.size());
    }

    @Then("the customer has {int} unused tokens left")
    public void thenTheCustomerHasUnusedTokens(Integer numberOfTokens) {
        assertNotNull("Customer must be registered before retrieving tokens", state.customer);
        try {
            state.tokens = customerService.retrieveTokens(state.customer);
        } catch (Exception e) {
            fail("Failed to request tokens: " + e.getMessage());
        }
        assertEquals("Expected to receive " + numberOfTokens + " tokens, but got " + state.tokens.size(),
                numberOfTokens.intValue(), state.tokens.size());
    }

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(Integer numberOfTokens) {
        try {
            state.tokens = customerService.requestMoreTokens(state.customer, numberOfTokens);
        } catch (Exception e) {
            fail("Failed to request tokens: " + e.getMessage());
        }
    }
}
