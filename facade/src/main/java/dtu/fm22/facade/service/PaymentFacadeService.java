package dtu.fm22.facade.service;

import com.google.gson.reflect.TypeToken;
import dtu.fm22.facade.record.PaymentRequest;
import dtu.fm22.facade.record.TokenValidationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import dtu.fm22.facade.record.Payment;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

    public Payment createPayment(PaymentRequest paymentRequest) {
        var correlationId = UUID.randomUUID();
        paymentInProgress.put(correlationId, new CompletableFuture<>());

        var paymentInfoRequestedEvent = new Event(TopicNames.PAYMENT_INFO_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentInfoRequestedEvent);

        var paymentRequestedEvent = new Event(TopicNames.PAYMENT_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentRequestedEvent);

        var validationRequest = new TokenValidationRequest(paymentRequest.token(), paymentRequest.customerId());
        var validationEvent = new Event(TopicNames.TOKEN_VALIDATION_REQUESTED, validationRequest, correlationId);
        queue.publish(validationEvent);

        return paymentInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
    }

    public Optional<Payment> getPaymentById(String id) {
        var correlationId = UUID.fromString(id);
        transactionInProgress.put(correlationId, new CompletableFuture<>());
        var transactionRequestedEvent = new Event(TopicNames.TRANSACTION_REQUESTED, id, correlationId);
        queue.publish(transactionRequestedEvent);

        return Optional.ofNullable(transactionInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join());
    }

    private void handlePaymentCreated(Event event) {
        var payment = event.getArgument(0, Payment.class);
        var correlationId = event.getArgument(1, UUID.class);

        var future = paymentInProgress.get(correlationId);
        if (future != null) {
            future.complete(payment);
//            paymentInProgress.remove(correlationId);
        }
    }

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
