package dtu.fm22.e2e.steps;

import dtu.fm22.e2e.SharedState;
import dtu.fm22.e2e.service.PaymentService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class PaymentServiceSteps {
    private final SharedState state;
    private final PaymentService paymentService = new PaymentService();

    public boolean successful = false;
    public String errorMessage;

    public PaymentServiceSteps(SharedState state) {
        this.state = state;
    }

    @When("the customer initiates a payment for {string} kr using a token")
    public void theMerchantInitiatesAPaymentForKrByTheCustomer(String amount) {
        assertNotNull(state.tokens);
        assertFalse("Customer must have state.tokens to make a payment", state.tokens.isEmpty());
        var token = state.tokens.getFirst();
        try {
            successful = paymentService.pay(amount, state.merchant.id, token);
        } catch (Exception e) {
            successful = false;
            errorMessage = e.getMessage();
        }
        state.tokens.remove(token);
    }

    @Then("the payment is successful")
    public void thePaymentIsSuccessful() {
        assertTrue("Expected payment to be successful, but it failed with error: " + errorMessage, successful);
    }
    @Then("the balance of the customer at the bank is {string} kr")
    public void theBalanceOfTheCustomerAtTheBankIsKr(String amount) throws Exception {
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        BigDecimal balance = state.bank.getAccount(state.customer.bankId).getBalance();
        assertEquals(
                "Expected balance " + amountBigDecimal + " but was " + balance,
                0, amountBigDecimal.compareTo(balance)
        );
    }

    @Then("the balance of the merchant at the bank is {string} kr")
    public void theBalanceOfTheMerchantAtTheBankIsKr(String amount) throws Exception {
        BigDecimal amountBigDecimal = new BigDecimal(amount);
        BigDecimal balance = state.bank.getAccount(state.merchant.bankId).getBalance();
        assertEquals(
                "Expected balance " + amountBigDecimal + " but was " + balance,
                0, amountBigDecimal.compareTo(balance)
        );
    }

    @When("the customer initiates a payment for {string} kr to an unknown merchant using a token")
    public void theCustomerInitiatesAPaymentToUnknownMerchantUsingAToken(String amount) {
        assertNotNull(state.tokens);
        assertFalse("Customer must have tokens to make a payment", state.tokens.isEmpty());
        var token = state.tokens.getFirst();
        try {
            // Use a random UUID as the unknown merchant ID
            successful = paymentService.pay(amount, UUID.randomUUID(), token);
        } catch (Exception e) {
            successful = false;
            errorMessage = e.getMessage();
        }
        state.tokens.remove(token);
    }

    @Then("the payment fails")
    public void thePaymentFails() {
        assertFalse("Expected payment to fail, but it succeeded", successful);
    }

    @Then("the error message is {string}")
    public void theErrorMessageIs(String expectedMessage) {
        assertNotNull("Error message should not be null", errorMessage);
        assertEquals(expectedMessage, errorMessage);
    }

    @Then("the error message contains {string}")
    public void theErrorMessageContains(String expectedMessage) {
        assertNotNull("Error message should not be null", errorMessage);
        assertTrue("Expected error message to contain '" + expectedMessage + "' but was: " + errorMessage,
                errorMessage.toLowerCase().contains(expectedMessage.toLowerCase()));
    }

    @When("the customer initiates a payment for {string} kr using an invalid token")
    public void theCustomerInitiatesAPaymentUsingAnInvalidToken(String amount) {
        var invalidToken = "invalid-token-" + UUID.randomUUID();
        try {
            successful = paymentService.pay(amount, state.merchant.id, invalidToken);
        } catch (Exception e) {
            successful = false;
            errorMessage = e.getMessage();
        }
    }

}
