package dtu.sdws26.gr22.pay.service.service;

import dtu.sdws26.gr22.pay.service.record.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public final class CustomerService {
    private final Map<UUID, CompletableFuture<Customer>> customersInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    @Inject
    public CustomerService(MessageQueue queue) {
        this.queue = queue;
        queue.addHandler(TopicNames.CUSTOMER_REGISTRATION_COMPLETED, this::handleCustomerRegistrationCompleted);
        queue.addHandler(TopicNames.CUSTOMER_INFO_PROVIDED, this::handleCustomerInfoProvided);
    }

    public Customer register(Customer customer) {
        var id = UUID.randomUUID();

        customersInProgress.put(id, new CompletableFuture<>());
        var event = new Event(TopicNames.CUSTOMER_REGISTRATION_REQUESTED, customer, id);
        queue.publish(event);

        return customersInProgress.get(id).join();
    }

    public void unregister(String id) {
        var event = new Event(TopicNames.CUSTOMER_UNREGISTRATION_REQUESTED, id);
        queue.publish(event);
    }

    public Optional<Customer> getById(String id) {
        var correlationId = UUID.randomUUID();
        customersInProgress.put(correlationId, new CompletableFuture<>());
        var event = new Event(TopicNames.CUSTOMER_INFO_REQUESTED, id, correlationId);
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
