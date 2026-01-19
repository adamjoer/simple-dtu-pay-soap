package dtu.sdws26.gr22.pay.service.service;

import com.google.gson.reflect.TypeToken;
import dtu.sdws26.gr22.pay.service.record.PaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;
import dtu.sdws26.gr22.pay.service.record.Payment;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public final class PaymentService {

    private final Map<UUID, CompletableFuture<Payment>> paymentInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Payment>> transactionInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Collection<Payment>>> transactionHistoryInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    @Inject
    public PaymentService(MessageQueue queue) {
        this.queue = queue;
        queue.addHandler(TopicNames.PAYMENT_CREATED, this::handlePaymentCreated);
        queue.addHandler(TopicNames.TRANSACTION_PROVIDED, this::handleTransactionProvided);
        queue.addHandler(TopicNames.TRANSACTION_ALL_HISTORY_PROVIDED, this::handleAllHistoryProvided);
    }

    public Payment createPayment(PaymentRequest paymentRequest) {
        var correlationId = UUID.randomUUID();
        paymentInProgress.put(correlationId, new CompletableFuture<>());

        var paymentInfoRequestedEvent = new Event(TopicNames.PAYMENT_INFO_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentInfoRequestedEvent);

        var paymentRequestedEvent = new Event(TopicNames.PAYMENT_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentRequestedEvent);

        return paymentInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
    }

    public Optional<Payment> getPaymentById(String id) {
        var correlationId = UUID.fromString(id);
        transactionInProgress.put(correlationId, new CompletableFuture<>());
        var transactionRequestedEvent = new Event(TopicNames.TRANSACTION_REQUESTED, id, correlationId);
        queue.publish(transactionRequestedEvent);

        return Optional.ofNullable(transactionInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join());
    }

    public Collection<Payment> getAllPayments() {
        var correlationId = UUID.randomUUID();
        transactionHistoryInProgress.put(correlationId, new CompletableFuture<>());
        var transactionRequestedEvent = new Event(TopicNames.TRANSACTION_ALL_HISTORY_REQUESTED, correlationId);
        queue.publish(transactionRequestedEvent);

        return transactionHistoryInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
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

    private void handleAllHistoryProvided(Event event) {
        var collectionType = new TypeToken<Collection<Payment>>() {
        };

        Collection<Payment> payments = event.getArgument(0, collectionType);
        var correlationId = event.getArgument(1, UUID.class);

        var future = transactionHistoryInProgress.get(correlationId);
        if (future != null) {
            future.complete(payments);
//            transactionHistoryInProgress.remove(correlationId);
        }
    }
}
