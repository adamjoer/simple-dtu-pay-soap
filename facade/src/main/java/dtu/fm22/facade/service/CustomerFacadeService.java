package dtu.fm22.facade.service;

import com.google.gson.reflect.TypeToken;
import dtu.fm22.facade.exceptions.ExceptionFactory;
import dtu.fm22.facade.record.Customer;
import dtu.fm22.facade.record.TokenRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;
import messaging.implementations.RabbitMqResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@ApplicationScoped
public final class CustomerFacadeService {
    private final Map<UUID, CompletableFuture<Customer>> customersInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<List<String>>> tokenRequestsInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    @Inject
    public CustomerFacadeService(MessageQueue queue) {
        this.queue = queue;
        queue.addHandler(TopicNames.CUSTOMER_REGISTRATION_COMPLETED, this::handleCustomerRegistrationCompleted);
        queue.addHandler(TopicNames.CUSTOMER_INFO_PROVIDED, this::handleCustomerInfoProvided);
        queue.addHandler(TopicNames.CUSTOMER_TOKEN_PROVIDED, this::handleTokenProvided);
        queue.addHandler(TopicNames.CUSTOMER_TOKEN_REPLENISH_COMPLETED, this::handleTokenProvided);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public Customer register(Customer customer) {
        var id = UUID.randomUUID();

        customersInProgress.put(id, new CompletableFuture<>());
        var event = new Event(TopicNames.CUSTOMER_REGISTRATION_REQUESTED, customer, id);
        queue.publish(event);

        return customersInProgress.get(id).orTimeout(5, TimeUnit.SECONDS).join();
    }

    public void unregister(String id) {
        var event = new Event(TopicNames.CUSTOMER_UNREGISTRATION_REQUESTED, id);
        queue.publish(event);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public Optional<Customer> getById(String id) {
        var correlationId = UUID.randomUUID();
        customersInProgress.put(correlationId, new CompletableFuture<>());
        var event = new Event(TopicNames.CUSTOMER_INFO_REQUESTED, id, correlationId);
        queue.publish(event);

        return Optional.ofNullable(customersInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join());
    }

    /**
     * @author s200718, s205135, s232268
     */
    private void handleCustomerRegistrationCompleted(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var correlationId = event.getArgument(1, UUID.class);

        customersInProgress.get(correlationId).complete(customer);
    }

    /**
     * @author s200718, s205135, s232268
     */
    private void handleCustomerInfoProvided(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var correlationId = event.getArgument(1, UUID.class);

        customersInProgress.get(correlationId).complete(customer);
    }

    public List<String> getTokens(String customerId) {
        var correlationId = UUID.randomUUID();
        tokenRequestsInProgress.put(correlationId, new CompletableFuture<>());

        var event = new Event(TopicNames.CUSTOMER_TOKEN_REQUESTED, customerId, correlationId);
        queue.publish(event);

        try {
            return tokenRequestsInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException)
                throw new InternalServerErrorException("Token retrieval timed out");
            if (e.getCause() instanceof WebApplicationException)
                throw (WebApplicationException) e.getCause();
            throw e;
        }
    }

    public List<String> requestTokens(String customerId, int numberOfTokens) {
        var correlationId = UUID.randomUUID();
        tokenRequestsInProgress.put(correlationId, new CompletableFuture<>());
        var tokenRequest = new TokenRequest(customerId, numberOfTokens);
        var event = new Event(TopicNames.CUSTOMER_TOKEN_REPLENISH_REQUESTED, tokenRequest, correlationId);
        queue.publish(event);

        try {
            return tokenRequestsInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException)
                throw new InternalServerErrorException("Token request timed out");
            if (e.getCause() instanceof WebApplicationException)
                throw (WebApplicationException) e.getCause();
            throw e;
        }
    }

    private void handleTokenProvided(Event event) {
        var collectionType = new TypeToken<List<String>>() {
        };
        RabbitMqResponse<List<String>> tokensResponse = event.getArgumentWithError(0, collectionType.getType());
        var correlationId = event.getArgument(1, UUID.class);

        var future = tokenRequestsInProgress.get(correlationId);
        if (future == null) {
            return;
        }

        if (tokensResponse.isError()) {
            future.completeExceptionally(ExceptionFactory.fromRabbitMqResponse(tokensResponse));
        } else {
            future.complete(tokensResponse.getData());
        }
    }

/*
    private void handleTokenReplenishCompleted(Event event) {
        var collectionType = new TypeToken<List<String>>() {
        };
        RabbitMqResponse<List<String>> tokensResponse = event.getArgumentWithError(0, collectionType.getType());
        var correlationId = event.getArgument(1, UUID.class);
        var future = tokenRequestsInProgress.get(correlationId);
        if (future == null) {
            return;
        }

        if (tokensResponse.isError()) {
            future.completeExceptionally(ExceptionFactory.fromRabbitMqResponse(tokensResponse));
        } else {
            future.complete(tokensResponse.getData());
        }
    }
*/
}
