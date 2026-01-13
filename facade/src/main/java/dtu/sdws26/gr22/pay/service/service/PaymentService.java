package dtu.sdws26.gr22.pay.service.service;

import dtu.sdws26.gr22.pay.service.record.PaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;
import dtu.sdws26.gr22.pay.service.record.Payment;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PaymentService {
    public static final String PAYMENT_REQUESTED = "PaymentRequested";
    public static final String PAYMENT_INFO_REQUESTED = "PaymentInfoRequested";

    private final MessageQueue queue;

    private final Map<UUID, CompletableFuture<Payment>> paymentInProgress = new ConcurrentHashMap<>();

    @Inject
    public PaymentService(MessageQueue queue) {
        this.queue = queue;
    }

    public UUID createPayment(PaymentRequest paymentRequest) {
        var correlationId = UUID.randomUUID();
        paymentInProgress.put(correlationId, new CompletableFuture<>());

        var paymentInfoRequestedEvent = new Event(PAYMENT_INFO_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentInfoRequestedEvent);

        var paymentRequestedEvent = new Event(PAYMENT_REQUESTED, paymentRequest, correlationId);
        queue.publish(paymentRequestedEvent);

        // TOOD: Wait for payment to be processed and return the payment ID
        return correlationId;
    }

    public Optional<Payment> getPaymentById(String id) {
        return Optional.empty();
    }

    public Collection<Payment> getAllPayments() {
        return new ArrayList<>();
    }
}
