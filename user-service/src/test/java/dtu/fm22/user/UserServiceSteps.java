package dtu.fm22.user;

import dtu.fm22.user.record.Customer;
import dtu.fm22.user.record.Merchant;
import dtu.fm22.user.record.PaymentInfoRequest;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class UserServiceSteps {

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
    
    private UserService service = new UserService(queue);
    private UUID correlationId;
    private Customer registeredCustomer;
    private Merchant registeredMerchant;

    @Before
    public void setUp() {
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID();
    }

    @Given("a registered customer")
    public void aRegisteredCustomer() {
        publishedEvent = new CompletableFuture<>();
        var customer = new Customer(null, "John", "Doe", "123456-7890", "bank123");
        var event = new Event(TopicNames.CUSTOMER_REGISTRATION_REQUESTED, customer, correlationId);
        service.handleRegisterCustomer(event);
        
        var responseEvent = publishedEvent.join();
        registeredCustomer = responseEvent.getArgument(0, Customer.class);
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID();
    }

    @Given("a registered customer and merchant")
    public void aRegisteredCustomerAndMerchant() {
        // Register customer
        publishedEvent = new CompletableFuture<>();
        var customer = new Customer(null, "John", "Doe", "123456-7890", "bank123");
        var customerEvent = new Event(TopicNames.CUSTOMER_REGISTRATION_REQUESTED, customer, correlationId);
        service.handleRegisterCustomer(customerEvent);
        registeredCustomer = publishedEvent.join().getArgument(0, Customer.class);
        
        // Register merchant
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID();
        var merchant = new Merchant(null, "Jane", "Smith", "098765-4321", "bank456");
        var merchantEvent = new Event(TopicNames.MERCHANT_REGISTRATION_REQUESTED, merchant, correlationId);
        service.handleRegisterMerchant(merchantEvent);
        registeredMerchant = publishedEvent.join().getArgument(0, Merchant.class);
        
        publishedEvent = new CompletableFuture<>();
        correlationId = UUID.randomUUID();
    }

    @When("a {string} event for a new customer is received")
    public void customerRegistrationRequested(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var customer = new Customer(null, "Test", "Customer", "111111-1111", "testbank");
        var event = new Event(eventType, customer, correlationId);
        service.handleRegisterCustomer(event);
    }

    @When("a {string} event for a new merchant is received")
    public void merchantRegistrationRequested(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var merchant = new Merchant(null, "Test", "Merchant", "222222-2222", "merchantbank");
        var event = new Event(eventType, merchant, correlationId);
        service.handleRegisterMerchant(event);
    }

    @When("a {string} event is received for that customer")
    public void customerInfoRequested(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var event = new Event(eventType, registeredCustomer.id().toString(), correlationId);
        service.handleCustomerInfoRequested(event);
    }

    @When("a {string} event is received for non-existing customer")
    public void customerInfoRequestedNonExisting(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var nonExistingId = UUID.randomUUID().toString();
        var event = new Event(eventType, nonExistingId, correlationId);
        service.handleCustomerInfoRequested(event);
    }

    @When("a {string} event is received")
    public void paymentInfoRequested(String eventType) {
        publishedEvent = new CompletableFuture<>();
        var request = new PaymentInfoRequest(
                registeredCustomer.id().toString(),
                registeredMerchant.id().toString()
        );
        var event = new Event(eventType, request, correlationId);
        service.handlePaymentInfoRequested(event);
    }

    @Then("the {string} event is sent")
    public void eventIsSent(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
    }

    @Then("the customer has a non-empty id")
    public void customerHasNonEmptyId() {
        Event event = publishedEvent.join();
        var customer = event.getArgument(0, Customer.class);
        assertNotNull("Customer ID should not be null", customer.id());
    }

    @Then("the merchant has a non-empty id")
    public void merchantHasNonEmptyId() {
        Event event = publishedEvent.join();
        var merchant = event.getArgument(0, Merchant.class);
        assertNotNull("Merchant ID should not be null", merchant.id());
    }

    @Then("the {string} event is sent with customer data")
    public void eventIsSentWithCustomerData(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var customer = event.getArgument(0, Customer.class);
        assertNotNull("Customer should not be null", customer);
        assertEquals(registeredCustomer.id(), customer.id());
        assertEquals(registeredCustomer.firstName(), customer.firstName());
    }

    @Then("the {string} event is sent with null customer")
    public void eventIsSentWithNullCustomer(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var customer = event.getArgument(0, Customer.class);
        assertNull("Customer should be null for non-existing customer", customer);
    }

    @Then("the {string} event is sent with customer and merchant data")
    public void eventIsSentWithCustomerAndMerchantData(String eventType) {
        Event event = publishedEvent.join();
        assertEquals(eventType, event.getTopic());
        
        var customer = event.getArgument(0, Customer.class);
        var merchant = event.getArgument(1, Merchant.class);
        
        assertNotNull("Customer should not be null", customer);
        assertNotNull("Merchant should not be null", merchant);
        assertEquals(registeredCustomer.id(), customer.id());
        assertEquals(registeredMerchant.id(), merchant.id());
    }
}
