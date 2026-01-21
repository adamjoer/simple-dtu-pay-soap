package dtu.fm22.token;

import dtu.fm22.token.record.TokenRequest;
import dtu.fm22.token.record.TokenValidationRequest;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class TokenServiceSteps {

    private CompletableFuture<Event> publishedEvent;
    
    private MessageQueue queue = new MessageQueue() {
        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }

        @Override
        public void addHandler(String eventType, Consumer<Event> handler) {
        }
    };
    
    private TokenService service = new TokenService(queue);
    private UUID customerId;
    private UUID correlationId;
    private String tokenValue;

    @Before
    public void setUp() {
        publishedEvent = new CompletableFuture<>();
        customerId = UUID.randomUUID();
        correlationId = UUID.randomUUID();
    }

    @Given("a customer has {int} unused tokens")
    public void aCustomerHasUnusedTokens(int count) {
        // Request tokens for the customer (must be <= 5)
        publishedEvent = new CompletableFuture<>();
        var request = new TokenRequest(customerId.toString(), count);
        var event = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_REQUESTED, request, correlationId);
        service.handleTokenReplenishRequested(event);
        publishedEvent.join(); // Wait for it to complete
        publishedEvent = new CompletableFuture<>(); // Reset for the next event
        correlationId = UUID.randomUUID(); // New correlation ID for the actual test
    }

    @Given("a customer has {int} unused token")
    public void aCustomerHasUnusedToken(int count) {
        aCustomerHasUnusedTokens(count);
    }

    @Given("a customer has a valid unused token")
    public void aCustomerHasAValidUnusedToken() {
        // Request 1 token for the customer
        publishedEvent = new CompletableFuture<>();
        var request = new TokenRequest(customerId.toString(), 1);
        var event = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_REQUESTED, request, correlationId);
        service.handleTokenReplenishRequested(event);
        
        // Get the token value from the response
        var responseEvent = publishedEvent.join();
        @SuppressWarnings("unchecked")
        List<String> tokens = responseEvent.getArgument(0, List.class);
        tokenValue = tokens.get(0);
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID();
    }

    @Given("a customer has a used token")
    public void aCustomerHasAUsedToken() {
        // First create a token
        aCustomerHasAValidUnusedToken();
        
        // Mark it as used
        var markUsedEvent = new Event(TopicNames.TOKEN_MARK_USED_REQUESTED, tokenValue, correlationId);
        service.handleTokenMarkUsedRequested(markUsedEvent);
        publishedEvent.join(); // Wait for completion
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID();
    }

    @When("a {string} event for customer requesting {int} tokens is received")
    public void tokenReplenishRequested(String eventType, int count) {
        publishedEvent = new CompletableFuture<>();
        var request = new TokenRequest(customerId.toString(), count);
        var event = new Event(eventType, request, correlationId);
        service.handleTokenReplenishRequested(event);
    }

    @When("a {string} event for that customer requesting {int} tokens is received")
    public void tokenReplenishRequestedForThatCustomer(String eventType, int count) {
        tokenReplenishRequested(eventType, count);
    }

    @When("a {string} event is received for that token")
    public void tokenEventReceivedForThatToken(String eventType) {
        publishedEvent = new CompletableFuture<>();
        if (eventType.equals(TopicNames.TOKEN_VALIDATION_REQUESTED)) {
            var validationRequest = new TokenValidationRequest(tokenValue);
            var event = new Event(eventType, validationRequest, correlationId);
            service.handleTokenValidationRequested(event);
        } else if (eventType.equals(TopicNames.TOKEN_MARK_USED_REQUESTED)) {
            var event = new Event(eventType, tokenValue, correlationId);
            service.handleTokenMarkUsedRequested(event);
        }
    }

    @Then("the {string} event is sent")
    public void eventIsSent(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
    }

    @Then("the event contains {int} unique tokens")
    public void eventContainsUniqueTokens(int count) {
        Event event = publishedEvent.join();
        
        @SuppressWarnings("unchecked")
        List<String> tokens = event.getArgument(0, List.class);
        assertEquals(count, tokens.size());
        assertEquals("Tokens should be unique", count, tokens.stream().distinct().count());
    }

    @Then("the {string} event is sent with error message")
    public void eventIsSentWithError(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        // Check that the first argument is a String (error message) rather than a List
        var argument = event.getArgument(0, Object.class);
        assertTrue("Expected error message as String", argument instanceof String);
    }

    @Then("the {string} event is sent with valid=true and customerId")
    public void eventIsSentWithValidTrue(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var isValid = event.getArgument(0, Boolean.class);
        var returnedCustomerId = event.getArgument(1, String.class);
        
        assertTrue("Token should be valid", isValid);
        assertEquals(customerId.toString(), returnedCustomerId);
    }

    @Then("the {string} event is sent with valid=false")
    public void eventIsSentWithValidFalse(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var isValid = event.getArgument(0, Boolean.class);
        assertFalse("Token should be invalid", isValid);
    }

    @Then("the {string} event is sent with success=true")
    public void eventIsSentWithSuccessTrue(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var success = event.getArgument(0, Boolean.class);
        assertTrue("Mark used should succeed", success);
    }

    @And("the token is marked as used")
    public void theTokenIsMarkedAsUsed() {
        // Verify by trying to validate the token - it should fail
        publishedEvent = new CompletableFuture<>();
        var validationRequest = new TokenValidationRequest(tokenValue);
        var event = new Event(TopicNames.TOKEN_VALIDATION_REQUESTED, validationRequest, UUID.randomUUID());
        service.handleTokenValidationRequested(event);
        
        var responseEvent = publishedEvent.join();
        assertEquals(TopicNames.TOKEN_VALIDATION_PROVIDED, responseEvent.getTopic());
        
        var isValid = responseEvent.getArgument(0, Boolean.class);
        assertFalse("Token should now be invalid (used)", isValid);
    }
}
