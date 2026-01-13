package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.exceptions.CustomerNotFoundException;
import dtu.sdws26.gr22.pay.service.exceptions.MerchantNotFoundException;
import dtu.sdws26.gr22.pay.service.record.Customer;
import dtu.sdws26.gr22.pay.service.record.Merchant;
import dtu.sdws26.gr22.pay.service.record.PaymentRequest;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    public static final String PAYMENT_INFO_REQUESTED = "PaymentInfoRequested";
    public static final String PAYMENT_INFO_PROVIDED = "PaymentInfoProvided";

    private final ConcurrentHashMap<UUID, Customer> customers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Merchant> merchants = new ConcurrentHashMap<>();


    private MessageQueue queue;

    public UserService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(PAYMENT_INFO_REQUESTED, this::handlePaymentInfoRequested);
    }

    public UUID registerCustomer(String firstName, String lastName, String cprNumber, String bankId) {
        var id = UUID.randomUUID();
        var customer = new Customer(id, firstName, lastName, cprNumber, bankId);
        customers.put(id, customer);
        return id;
    }

    public void unregisterCustomer(String id) {
        try {
            var uuid = UUID.fromString(id);
            if (!customers.containsKey(uuid)) {
                throw new MerchantNotFoundException(uuid.toString());
            }
            customers.remove(uuid);
        } catch (IllegalArgumentException e) {
            throw new CustomerNotFoundException(id);
        }
    }

    public Optional<Customer> getByCustomerId(String id) {
        try {
            var uuid = UUID.fromString(id);
            var customer = customers.get(uuid);
            return Optional.ofNullable(customer);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public UUID registerMerchant(String firstName, String lastName, String cprNumber, String bankId) {
        var id = UUID.randomUUID();
        var merchant = new Merchant(id, firstName, lastName, cprNumber, bankId);
        merchants.put(id, merchant);
        return id;
    }

    public void unregisterMerchant(String id) {
        try {
            var uuid = UUID.fromString(id);
            if (!merchants.containsKey(uuid)) {
                throw new MerchantNotFoundException(uuid.toString());
            }
            merchants.remove(uuid);
        } catch (IllegalArgumentException e) {
            throw new MerchantNotFoundException(id);
        }
    }

    public Optional<Merchant> getMerchantById(String id) {
        try {
            var uuid = UUID.fromString(id);
            var merchant = merchants.get(uuid);
            return Optional.ofNullable(merchant);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private void handlePaymentInfoRequested(Event event) {
        var paymentRequest = event.getArgument(0, PaymentRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

        try {
            var customer = getByCustomerId(paymentRequest.customerId()).orElse(null);
            var merchant = getMerchantById(paymentRequest.merchantId()).orElse(null);

            var paymentInfoProvidedEvent = new Event(PAYMENT_INFO_PROVIDED, customer, merchant, correlationId);
            queue.publish(paymentInfoProvidedEvent);

        } catch (IllegalArgumentException e) {
            System.err.println("handlePaymentInfoRequested: Invalid UUID format in payment request");
        }
    }

}
