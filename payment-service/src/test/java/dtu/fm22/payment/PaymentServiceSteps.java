package dtu.fm22.payment;

import com.google.gson.reflect.TypeToken;
import dtu.fm22.payment.record.*;
import dtu.ws.fastmoney.BankService;
import io.cucumber.java.Before;
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
import static org.mockito.Mockito.*;

public class PaymentServiceSteps {

    private CompletableFuture<Event> publishedEvent;
    private BankService bankService;
    
    private MessageQueue queue = new MessageQueue() {
        @Override
        public void publish(Event event) {
            publishedEvent.complete(event);
        }

        @Override
        public void addHandler(String eventType, Consumer<Event> handler) {
        }
    };
    
    private PaymentService service;
    private UUID correlationId;
    private Customer testCustomer;
    private Merchant testMerchant;
    private String testToken;

    @Before
    public void setUp() {
        publishedEvent = new CompletableFuture<>();
        bankService = mock(BankService.class);
        service = new PaymentService(queue, bankService);
        correlationId = UUID.randomUUID();
        testToken = "test-token-" + UUID.randomUUID();
        
        testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", "123456-7890", "customer-bank-id");
        testMerchant = new Merchant(UUID.randomUUID(), "Jane", "Smith", "098765-4321", "merchant-bank-id");
    }

    @Given("a pending payment request")
    public void aPendingPaymentRequest() {
        // Simulate a payment request being received and stored
        publishedEvent = new CompletableFuture<>();
        var paymentRequest = new PaymentRequest(testMerchant.id().toString(), "100.00", testToken);
        var event = new Event(TopicNames.PAYMENT_REQUESTED, paymentRequest, correlationId);
        service.handlePaymentRequested(event);
        publishedEvent.join(); // Wait for token validation request to be published
        publishedEvent = new CompletableFuture<>(); // Reset for next event
    }

    @Given("completed payments for a customer")
    public void completedPaymentsForACustomer() throws Exception {
        // Create a completed payment by simulating the full flow
        createCompletedPayment();
    }

    @Given("completed payments for a merchant")
    public void completedPaymentsForAMerchant() throws Exception {
        // Create a completed payment by simulating the full flow
        createCompletedPayment();
    }

    private void createCompletedPayment() throws Exception {
        // Step 1: Payment requested
        publishedEvent = new CompletableFuture<>();
        var paymentRequest = new PaymentRequest(testMerchant.id().toString(), "100.00", testToken);
        var paymentRequestEvent = new Event(TopicNames.PAYMENT_REQUESTED, paymentRequest, correlationId);
        service.handlePaymentRequested(paymentRequestEvent);
        publishedEvent.join(); // Wait for token validation request
        
        // Step 2: Token validation provided (valid)
        publishedEvent = new CompletableFuture<>();
        var tokenValidationEvent = new Event(TopicNames.TOKEN_VALIDATION_PROVIDED, 
                true, testCustomer.id().toString(), "Token is valid", correlationId);
        service.handleTokenValidationProvided(tokenValidationEvent);
        publishedEvent.join(); // Wait for payment info request
        
        // Step 3: Payment info provided
        publishedEvent = new CompletableFuture<>();
        var paymentInfoEvent = new Event(TopicNames.PAYMENT_INFO_PROVIDED, 
                testCustomer, testMerchant, correlationId);
        service.handlePaymentInfoProvided(paymentInfoEvent);
        publishedEvent.join(); // Wait for payment to complete
        
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID(); // New correlation ID for the report request
    }

    @When("a {string} event is received")
    public void eventIsReceived(String eventType) {
        publishedEvent = new CompletableFuture<>();
        if (eventType.equals(TopicNames.PAYMENT_REQUESTED)) {
            var paymentRequest = new PaymentRequest(testMerchant.id().toString(), "100.00", testToken);
            var event = new Event(eventType, paymentRequest, correlationId);
            service.handlePaymentRequested(event);
        } else if (eventType.equals(TopicNames.CUSTOMER_REPORT_REQUESTED)) {
            var event = new Event(eventType, testCustomer, correlationId);
            service.handleCustomerReportRequested(event);
        } else if (eventType.equals(TopicNames.MERCHANT_REPORT_REQUESTED)) {
            var event = new Event(eventType, testMerchant, correlationId);
            service.handleMerchantReportRequested(event);
        }
    }

    @When("a {string} event with valid=true is received")
    public void tokenValidationProvidedValid(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var event = new Event(eventType, true, testCustomer.id().toString(), "Token is valid", correlationId);
        service.handleTokenValidationProvided(event);
    }

    @When("a {string} event with valid=false is received")
    public void tokenValidationProvidedInvalid(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var event = new Event(eventType, false, null, "Token is invalid", correlationId);
        service.handleTokenValidationProvided(event);
    }

    @Then("a {string} event is published")
    public void eventIsPublished(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
    }

    @Then("a {string} event is published with error")
    public void eventIsPublishedWithError(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var response = event.getArgumentWithError(0, Payment.class);
        assertTrue("Expected error status code", response.statusCode() >= 400);
    }

    @Then("a {string} event with filtered payments is published")
    public void customerReportEventWithFilteredPayments(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        List<Payment> payments = event.getArgument(0, new TypeToken<List<Payment>>(){});
        assertNotNull("Payments list should not be null", payments);
        
        // All payments should belong to the customer
        for (Payment payment : payments) {
            assertEquals("Payment should belong to the customer", 
                    testCustomer.id(), payment.customer().id());
        }
    }

    @Then("a {string} event with obfuscated customer is published")
    public void merchantReportEventWithObfuscatedCustomer(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        List<Payment> payments = event.getArgument(0, new TypeToken<List<Payment>>(){});
        assertNotNull("Payments list should not be null", payments);
        
        // All payments should have obfuscated customer (null)
        for (Payment payment : payments) {
            assertNull("Customer should be obfuscated (null)", payment.customer());
            assertEquals("Payment should belong to the merchant", 
                    testMerchant.id(), payment.merchant().id());
        }
    }
}
