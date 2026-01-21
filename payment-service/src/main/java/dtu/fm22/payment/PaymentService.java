package dtu.fm22.payment;

import dtu.fm22.payment.record.*;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;
import messaging.implementations.RabbitMQResponse;

public class PaymentService {

    private final ConcurrentHashMap<UUID, Payment> payments = new ConcurrentHashMap<>();

    private final BankService bankService;

    private final Map<UUID, PaymentInfo> pendingPaymentInfo = new ConcurrentHashMap<>();
    private final Map<UUID, PaymentRequest> pendingPaymentRequests = new ConcurrentHashMap<>();
    private final Map<UUID, String> resolvedCustomerIds = new ConcurrentHashMap<>();
    private final Map<UUID, String> tokenValidationErrors = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    public PaymentService(MessageQueue queue) {
        this(queue, new BankService_Service().getBankServicePort());
    }

    public PaymentService(MessageQueue queue, BankService bankService) {
        this.queue = queue;
        this.bankService = bankService;
        this.queue.addHandler(TopicNames.PAYMENT_REQUESTED, this::handlePaymentRequested);
        this.queue.addHandler(TopicNames.PAYMENT_INFO_PROVIDED, this::handlePaymentInfoProvided);
        this.queue.addHandler(TopicNames.TOKEN_VALIDATION_PROVIDED, this::handleTokenValidationProvided);
        this.queue.addHandler(TopicNames.TRANSACTION_REQUESTED, this::handleTransactionRequested);
        this.queue.addHandler(TopicNames.CUSTOMER_REPORT_REQUESTED, this::handleCustomerReportRequested);
        this.queue.addHandler(TopicNames.MERCHANT_REPORT_REQUESTED, this::handleMerchantReportRequested);
        this.queue.addHandler(TopicNames.MANAGER_REPORT_PROVIDED, this::handleManagerReportProvided);
    }

    /**
     * @author s215206
     */
    public void handlePaymentRequested(Event event) {
        var paymentRequest = event.getArgument(0, PaymentRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

        // Store the payment request
        pendingPaymentRequests.put(correlationId, paymentRequest);

        // Request token validation (this will return customerId)
        var tokenValidationRequest = new TokenValidationRequest(paymentRequest.token());
        var tokenValidationEvent = new Event(TopicNames.TOKEN_VALIDATION_REQUESTED, tokenValidationRequest, correlationId);
        queue.publish(tokenValidationEvent);
    }

    /**
     * @author s215206
     */
    public void handleTokenValidationProvided(Event event) {
        var isValid = event.getArgument(0, Boolean.class);
        var customerId = event.getArgument(1, String.class);
        var message = event.getArgument(2, String.class);
        var correlationId = event.getArgument(3, UUID.class);

        if (!isValid) {
            // Token invalid - send error response immediately with generic message
            tokenValidationErrors.put(correlationId, message);
            var errorResponse = new RabbitMQResponse<Payment>(400, "Invalid or used token");
            var errorEvent = new Event(TopicNames.PAYMENT_CREATED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        // Token is valid - store customerId and request payment info
        resolvedCustomerIds.put(correlationId, customerId);

        var paymentRequest = pendingPaymentRequests.get(correlationId);
        if (paymentRequest != null) {
            // Step 2: Request user info using resolved customerId
            var paymentInfoRequest = new PaymentInfoRequest(customerId, paymentRequest.merchantId());
            var paymentInfoEvent = new Event(TopicNames.PAYMENT_INFO_REQUESTED, paymentInfoRequest, correlationId);
            queue.publish(paymentInfoEvent);
        }
    }

    public void handlePaymentInfoProvided(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var merchant = event.getArgument(1, Merchant.class);
        var correlationId = event.getArgument(2, UUID.class);

        var paymentInfo = new PaymentInfo(customer, merchant);
        pendingPaymentInfo.put(correlationId, paymentInfo);

        // Complete the payment
        tryCompletePayment(correlationId);
    }

    /**
     * @author s200718
     */
    private synchronized void tryCompletePayment(UUID correlationId) {
        if (payments.containsKey(correlationId)) {
            return;
        }

        var request = pendingPaymentRequests.get(correlationId);
        var info = pendingPaymentInfo.get(correlationId);
        var customerId = resolvedCustomerIds.get(correlationId);

        // Need all components to complete payment
        if (request == null || info == null || customerId == null) {
            return;
        }

        doPayment(info.customer(), info.merchant(), request.amount(), request.token(), correlationId);
    }

    /**
     * @author s200718
     */
    private void doPayment(Customer customer, Merchant merchant, String amount, String token, UUID paymentId) {
        var amountBigDecimal = new BigDecimal(amount);
        try {
            bankService.transferMoneyFromTo(customer.bankId(),
                    merchant.bankId(),
                    amountBigDecimal,
                    "from " + customer.firstName() + " to " + merchant.firstName());
        } catch (BankServiceException_Exception e) {
            System.err.println("doPayment: Bank transfer failed: " + e.getMessage());
            return;
        }

        var payment = new Payment(paymentId, customer, merchant, amountBigDecimal, token, Instant.now().toString());
        payments.put(paymentId, payment);

        // Mark token as used after successful payment
        if (token != null && !token.isEmpty()) {
            var markUsedEvent = new Event(TopicNames.TOKEN_MARK_USED_REQUESTED, token, paymentId);
            queue.publish(markUsedEvent);
        }

        Event paymentCreatedEvent = new Event(TopicNames.PAYMENT_CREATED, new RabbitMQResponse<>(payment), paymentId);
        queue.publish(paymentCreatedEvent);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public void handleTransactionRequested(Event event) {
        var id = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);
        try {
            var uuid = UUID.fromString(id);
            var payment = payments.get(uuid);

            var transactionProvidedEvent = new Event(TopicNames.TRANSACTION_PROVIDED, payment, correlationId);
            queue.publish(transactionProvidedEvent);
        } catch (IllegalArgumentException e) {
            System.err.println("handleTransactionsRequested: Invalid UUID string: " + id);
        }
    }

    /**
     * @author s200718, s205135
     */
    public void handleCustomerReportRequested(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var filteredList = payments.values()
                .stream()
                .filter(item -> item.customer().id().equals(customer.id()))
                .sorted(Comparator.comparing(Payment::timestamp)).toList();
        Event paymentCreatedEvent = new Event(TopicNames.CUSTOMER_REPORT_PROVIDED, filteredList, customer.id());
        queue.publish(paymentCreatedEvent);
    }

    /**
     * @author s200718, s205135
     */
    public void handleMerchantReportRequested(Event event) {
        var merchant = event.getArgument(0, Merchant.class);
        var filteredList = payments
                .values()
                .stream()
                .filter(item -> item.merchant().id().equals(merchant.id()))
                .map(Payment::withObfuscatedCustomer)
                .sorted(Comparator.comparing(Payment::timestamp))
                .toList();
        Event paymentCreatedEvent = new Event(TopicNames.MERCHANT_REPORT_PROVIDED, filteredList, merchant.id());
        queue.publish(paymentCreatedEvent);
    }

    /**
     * @author s200718, s205135
     */
    public void handleManagerReportProvided(Event event) {
        var correlationId = event.getArgument(0, UUID.class);
        var allPayments = payments
                .values()
                .stream()
                .sorted(Comparator.comparing(Payment::timestamp))
                .toList();
        Event paymentCreatedEvent = new Event(TopicNames.MANAGER_REPORT_PROVIDED, allPayments, correlationId);
        queue.publish(paymentCreatedEvent);
    }
}
