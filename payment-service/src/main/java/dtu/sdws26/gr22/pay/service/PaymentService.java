package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.record.Customer;
import dtu.sdws26.gr22.pay.service.record.Merchant;
import dtu.sdws26.gr22.pay.service.record.PaymentRequest;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import dtu.sdws26.gr22.pay.service.exceptions.CustomerNotFoundException;
import dtu.sdws26.gr22.pay.service.exceptions.MerchantNotFoundException;
import dtu.sdws26.gr22.pay.service.exceptions.PaymentException;
import dtu.sdws26.gr22.pay.service.record.Payment;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import messaging.Event;
import messaging.MessageQueue;

public class PaymentService {

    private static final String PAYMENT_CREATED = "PaymentCreated";
    private static final String PAYMENT_REQUESTED = "PaymentRequested";

    public static final String PAYMENT_INFO_PROVIDED = "PaymentInfoProvided";

    private static final String TRANSACTION_HISTORY_REQUESTED = "TransactionHistoryRequest";
    private static final String TRANSACTION_HISTORY_RESPONSE = "TransactionHistoryResponse";

    private static final String TRANSACTION_HISTORY_REQUESTED_ALL = "TransactionHistoryRequestAll";
    private static final String TRANSACTION_HISTORY_RESPONSE_ALL = "TransactionHistoryResponseAll";

    private static final String CUSTOMER_INFO_REQUESTED = "CustomerInfoRequest";
    private static final String CUSTOMER_INFO_RESPONSE = "CustomerInfoResponse";

    private static final String MERCHANT_INFO_REQUESTED = "MerchantInfoResponse";
    private static final String MERCHANT_INFO_RESPONSE = "MerchantInfoResponse";

    private final String API_KEY = System.getenv("SIMPLE_DTU_PAY_API_KEY");

    private final ConcurrentHashMap<UUID, Payment> payments = new ConcurrentHashMap<>();

    private final BankService bankService = new BankService_Service().getBankServicePort();

/*
    private final Map<UUID, CompletableFuture<PaymentInfo>> paymentInfoInProgress = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<PaymentRequest>> paymentRequestInProgress = new ConcurrentHashMap<>();
*/

    private MessageQueue queue;

    public PaymentService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(PAYMENT_REQUESTED, this::handlePaymentRequested);
        this.queue.addHandler(PAYMENT_INFO_PROVIDED, this::handlePaymentInfoProvided);
        this.queue.addHandler(TRANSACTION_HISTORY_REQUESTED_ALL, this::handleAllTransactionsRequested);
    }

    public void handlePaymentRequested(Event event) {
        var paymentRequest = event.getArgument(0, PaymentRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

    }

    private void handleTransactionsRequested(Event event) {
        Event paymentpublish = null;
        var id = event.getArgument(0, String.class);
        try {
            var uuid = UUID.fromString(id);
            var payment = payments.get(uuid);
            paymentpublish = new Event(TRANSACTION_HISTORY_RESPONSE_ALL, payment);
            queue.publish(paymentpublish);
        } catch (IllegalArgumentException e) {
            System.err.println("handleTransactionsRequested: Invalid UUID string: " + id);
        }

    }

    private void doPayment(Customer customer, Merchant merchant, String amount) {
        try {
            bankService.transferMoneyFromTo(customer.bankId(),
                    merchant.bankId(),
                    new BigDecimal(amount),
                    "from " + customer.firstName() + " to " + merchant.firstName());
        } catch (BankServiceException_Exception e) {
            System.err.println("doPayment: Bank transfer failed: " + e.getMessage());
            return;
        }

        var paymentId = UUID.randomUUID();
        var payment = new Payment(paymentId, customer, merchant, new BigDecimal(amount), Instant.now());
        payments.put(paymentId, payment);

        Event paymentCreatedEvent = new Event(PAYMENT_CREATED, payment);
        queue.publish(paymentCreatedEvent);
    }

    private void handleAllTransactionsRequested(Event event) {
        Event paymentCreatedEvent = new Event(TRANSACTION_HISTORY_RESPONSE_ALL, payments.values());
        queue.publish(paymentCreatedEvent);
    }

    private void handlePaymentInfoProvided(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var merchant = event.getArgument(1, Merchant.class);
        var correlationId = event.getArgument(2, UUID.class);


    }
