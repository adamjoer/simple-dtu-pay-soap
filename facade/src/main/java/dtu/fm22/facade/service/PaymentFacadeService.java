package dtu.fm22.facade.service;

import dtu.fm22.facade.exceptions.ExceptionFactory;
import dtu.fm22.facade.record.PaymentRequest;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.enterprise.context.ApplicationScoped;
import dtu.fm22.facade.record.Payment;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.*;
import java.util.concurrent.*;

@ApplicationScoped
public final class PaymentFacadeService {

    private final Map<UUID, CompletableFuture<Payment>> paymentInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Payment>> transactionInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Collection<Payment>>> transactionHistoryInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    @Inject
    public PaymentFacadeService(MessageQueue queue) {
        this.queue = queue;
        queue.addHandler(TopicNames.PAYMENT_CREATED, this::handlePaymentCreated);
        queue.addHandler(TopicNames.TRANSACTION_PROVIDED, this::handleTransactionProvided);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public Payment createPayment(PaymentRequest paymentRequest) {
        var correlationId = UUID.randomUUID();
        paymentInProgress.put(correlationId, new CompletableFuture<>());

        var paymentRequestedEvent = new Event(TopicNames.PAYMENT_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentRequestedEvent);

        try {
            return paymentInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException)
                throw new InternalServerErrorException("Payment processing timed out");
            if (e.getCause() instanceof WebApplicationException)
                throw (WebApplicationException) e.getCause();
            throw e;
        }
    }

    /**
     * @author s200718, s205135, s232268
     */
    public Optional<Payment> getPaymentById(String id) {
        var correlationId = UUID.fromString(id);
        transactionInProgress.put(correlationId, new CompletableFuture<>());
        var transactionRequestedEvent = new Event(TopicNames.TRANSACTION_REQUESTED, id, correlationId);
        queue.publish(transactionRequestedEvent);

        return Optional.ofNullable(transactionInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join());
    }

    /**
     * @author s200718, s205135, s232268
     */
    private void handlePaymentCreated(Event event) {
        var paymentResponse = event.getArgumentWithError(0, Payment.class);
        var correlationId = event.getArgument(1, UUID.class);

        var future = paymentInProgress.get(correlationId);
        if (future != null) {
            if (paymentResponse.isError())
                future.completeExceptionally(ExceptionFactory.fromRabbitMqResponse(paymentResponse));
            else
                future.complete(paymentResponse.getData());
        }
    }

    /**
     * @author s200718, s205135, s232268
     */
    private void handleTransactionProvided(Event event) {
        var payment = event.getArgument(0, Payment.class);
        var correlationId = event.getArgument(1, UUID.class);

        var future = transactionInProgress.get(correlationId);
        if (future != null) {
            future.complete(payment);
//            transactionInProgress.remove(correlationId);
        }
    }
}
