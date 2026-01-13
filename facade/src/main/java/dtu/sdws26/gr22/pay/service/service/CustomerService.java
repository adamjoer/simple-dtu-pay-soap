package dtu.sdws26.gr22.pay.service.service;

import dtu.sdws26.gr22.pay.service.record.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class CustomerService {
    public static final String CUSTOMER_REGISTRATION_REQUESTED = "CustomerRegistrationRequested";
    public static final String CUSTOMER_REGISTRATION_COMPLETED = "CustomerRegistrationCompleted";

    public static final String CUSTOMER_UNREGISTRATION_REQUESTED = "CustomerUnregistrationRequested";

    public static final String CUSTOMER_INFO_REQUESTED = "CustomerInfoRequested";
    public static final String CUSTOMER_INFO_PROVIDED = "CustomerInfoProvided";

    private final Map<UUID, CompletableFuture<Customer>> customersInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    public CustomerService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(CUSTOMER_REGISTRATION_COMPLETED, this::handleCustomerRegistrationCompleted);
        this.queue.addHandler(CUSTOMER_INFO_PROVIDED, this::handleCustomerInfoProvided);
    }

    public Customer register(Customer customer) {
        var id = UUID.randomUUID();

        customersInProgress.put(id, new CompletableFuture<>());
        var event = new Event(CUSTOMER_REGISTRATION_REQUESTED, customer, id);
        queue.publish(event);

        return customersInProgress.get(id).join();
    }

    public void unregister(String id) {
        var event = new Event(CUSTOMER_UNREGISTRATION_REQUESTED, id);
        queue.publish(event);
    }

    public Optional<Customer> getById(String id) {
        var correlationId = UUID.randomUUID();
        customersInProgress.put(correlationId, new CompletableFuture<>());
        var event = new Event(CUSTOMER_INFO_REQUESTED, id, correlationId);
        queue.publish(event);

        return Optional.ofNullable(customersInProgress.get(correlationId).join());
    }

    private void handleCustomerRegistrationCompleted(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var correlationId = event.getArgument(1, UUID.class);

        customersInProgress.get(correlationId).complete(customer);
    }

    private void handleCustomerInfoProvided(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var correlationId = event.getArgument(1, UUID.class);

        customersInProgress.get(correlationId).complete(customer);
    }
}
