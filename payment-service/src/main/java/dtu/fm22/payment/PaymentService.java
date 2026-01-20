package dtu.fm22.payment;

import dtu.fm22.payment.record.Customer;
import dtu.fm22.payment.record.Merchant;
import dtu.fm22.payment.record.PaymentRequest;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import dtu.fm22.payment.record.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

public class PaymentService {

    private final ConcurrentHashMap<UUID, Payment> payments = new ConcurrentHashMap<>();

    private final BankService bankService = new BankService_Service().getBankServicePort();

    private final Map<UUID, PaymentInfo> pendingPaymentInfo = new ConcurrentHashMap<>();
    private final Map<UUID, PaymentRequest> pendingPaymentRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> pendingTokenValidations = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    public PaymentService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(TopicNames.PAYMENT_REQUESTED, this::handlePaymentRequested);
        this.queue.addHandler(TopicNames.PAYMENT_INFO_PROVIDED, this::handlePaymentInfoProvided);
        this.queue.addHandler(TopicNames.TOKEN_VALIDATION_PROVIDED, this::handleTokenValidationProvided);
        this.queue.addHandler(TopicNames.TRANSACTION_REQUESTED, this::handleTransactionRequested);
        this.queue.addHandler(TopicNames.CUSTOMER_REPORT_REQUESTED, this::handleCustomerReportRequested);
        this.queue.addHandler(TopicNames.MERCHANT_REPORT_REQUESTED, this::handleMerchantReportRequested);
        this.queue.addHandler(TopicNames.MANAGER_REPORT_PROVIDED, this::handleManagerReportProvided);
    }

    private void handlePaymentRequested(Event event) {
        var paymentRequest = event.getArgument(0, PaymentRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

        pendingPaymentRequests.put(correlationId, paymentRequest);
        tryCompletePayment(correlationId);
    }

    private void handlePaymentInfoProvided(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var merchant = event.getArgument(1, Merchant.class);
        var correlationId = event.getArgument(2, UUID.class);

        var paymentInfo = new PaymentInfo(customer, merchant);
        pendingPaymentInfo.put(correlationId, paymentInfo);
        tryCompletePayment(correlationId);
    }

    private synchronized void tryCompletePayment(UUID correlationId) {
        if (payments.containsKey(correlationId)) {
            return;
        }

        var request = pendingPaymentRequests.get(correlationId);
        var info = pendingPaymentInfo.get(correlationId);
        var tokenValid = pendingTokenValidations.get(correlationId);

        if (request == null || info == null || tokenValid == null) {
            return;
        }

        if (!tokenValid) {
            System.err.println("tryCompletePayment: Token validation failed for payment request");
            // Could publish a payment failed event here
            return;
        }

        doPayment(info.customer(), info.merchant(), request.amount(), request.token(), correlationId);

        // Clean up
        // pendingPaymentRequests.remove(correlationId);
        // pendingPaymentInfo.remove(correlationId);
        // pendingTokenValidations.remove(correlationId);
    }

    private void handleTokenValidationProvided(Event event) {
        var isValid = event.getArgument(0, Boolean.class);
        var message = event.getArgument(1, String.class);
        var correlationId = event.getArgument(2, UUID.class);

        pendingTokenValidations.put(correlationId, isValid);
        tryCompletePayment(correlationId);
    }

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

        Event paymentCreatedEvent = new Event(TopicNames.PAYMENT_CREATED, payment, paymentId);
        queue.publish(paymentCreatedEvent);
    }

    private void handleTransactionRequested(Event event) {
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

    private void handleCustomerReportRequested(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var filteredList = payments.values()
                .stream()
                .filter(item -> item.customer().id().equals(customer.id()))
                .sorted(Comparator.comparing(Payment::timestamp)).toList();
        Event paymentCreatedEvent = new Event(TopicNames.CUSTOMER_REPORT_PROVIDED, filteredList, customer.id());
        queue.publish(paymentCreatedEvent);
    }

    private void handleMerchantReportRequested(Event event) {
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

    private void handleManagerReportProvided(Event event) {
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
