package dtu.fm22.e2e.steps;

import dtu.fm22.e2e.SharedState;
import dtu.fm22.e2e.service.PaymentService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.*;

public class TokenServiceSteps {

    private final SharedState state;

    private final PaymentService paymentService = new PaymentService();

    private boolean successful = false;
    private String errorMessage;

    public TokenServiceSteps(SharedState state) {
        this.state = state;
    }

    @When("the customer initiates a payment for {string} kr using an invalid token")
    public void theCustomerInitiatesAPaymentForKrUsingAnInvalidToken(String amount) {
        var token = "invalid-token";
        try {
            successful = paymentService.pay(amount, state.merchant.id, token);
        } catch (Exception e) {
            successful = false;
            errorMessage = e.getMessage();
        }
    }

    @Then("the payment fails")
    public void thePaymentFails() {
        assertFalse("Expected payment to fail, but it was successful", successful);
    }

    @Then("the error message is {string}")
    public void theErrorMessageIs(String message) {
        assertEquals(message, errorMessage);
    }
}
