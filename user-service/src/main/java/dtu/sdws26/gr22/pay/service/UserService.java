package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.exceptions.CustomerNotFoundException;
import dtu.sdws26.gr22.pay.service.exceptions.MerchantNotFoundException;
import dtu.sdws26.gr22.pay.service.record.Customer;
import dtu.sdws26.gr22.pay.service.record.Merchant;
import dtu.sdws26.gr22.pay.service.record.PaymentRequest;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final ConcurrentHashMap<UUID, Customer> customers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Merchant> merchants = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    public UserService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(TopicNames.PAYMENT_INFO_REQUESTED, this::handlePaymentInfoRequested);
        this.queue.addHandler(TopicNames.CUSTOMER_REGISTRATION_REQUESTED, this::handleRegisterCustomer);
        this.queue.addHandler(TopicNames.MERCHANT_REGISTRATION_REQUESTED, this::handleRegisterMerchant);
        this.queue.addHandler(TopicNames.CUSTOMER_UNREGISTRATION_REQUESTED, this::handleUnregisterCustomer);
        this.queue.addHandler(TopicNames.MERCHANT_UNREGISTRATION_REQUESTED, this::handleUnregisterMerchant);
        this.queue.addHandler(TopicNames.CUSTOMER_INFO_REQUESTED, this::handleCustomerInfoRequested);
        this.queue.addHandler(TopicNames.MERCHANT_INFO_REQUESTED, this::handleMerchantInfoRequested);
    }

    private Customer registerCustomer(Customer customer) {
        var id = UUID.randomUUID();
        customer = customer.withId(id);
        customers.put(id, customer);
        return customer;
    }

    private void handleRegisterCustomer(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var correlationId = event.getArgument(1, UUID.class);
        customer = registerCustomer(customer);
        var customerRegistrationEvent = new Event(TopicNames.CUSTOMER_REGISTRATION_COMPLETED, customer, correlationId);
        queue.publish(customerRegistrationEvent);
    }

    private void unregisterCustomer(String id) {
        try {
            var uuid = UUID.fromString(id);
            if (!customers.containsKey(uuid)) {
                throw new CustomerNotFoundException(uuid.toString());
            }
            customers.remove(uuid);
        } catch (IllegalArgumentException e) {
            throw new CustomerNotFoundException(id);
        }
    }

    private void handleUnregisterCustomer(Event event) {
        var id = event.getArgument(0, String.class);
        unregisterCustomer(id);
        var customerEvent = new Event(TopicNames.CUSTOMER_UNREGISTRATION_COMPLETED, id);
        queue.publish(customerEvent);
    }

    private Merchant registerMerchant(Merchant merchant) {
        var id = UUID.randomUUID();
        merchant = merchant.withId(id);
        merchants.put(id, merchant);
        return merchant;
    }

    private void handleRegisterMerchant(Event event) {
        var merchant = event.getArgument(0, Merchant.class);
        var correlationId = event.getArgument(1, UUID.class);
        merchant = registerMerchant(merchant);
        var merchantRegistrationEvent = new Event(TopicNames.MERCHANT_REGISTRATION_COMPLETED, merchant, correlationId);
        queue.publish(merchantRegistrationEvent);
    }

    private void unregisterMerchant(String id) {
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

    private void handleUnregisterMerchant(Event event) {
        var id = event.getArgument(0, String.class);
        unregisterMerchant(id);
        var merchantEvent = new Event(TopicNames.MERCHANT_UNREGISTRATION_COMPLETED, id);
        queue.publish(merchantEvent);
    }

    private void handleCustomerInfoRequested(Event event) {
        var customerId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);

        var customer = getByCustomerId(customerId).orElse(null);

        var customerInfoProvidedEvent = new Event(TopicNames.CUSTOMER_INFO_PROVIDED, customer, correlationId);
        queue.publish(customerInfoProvidedEvent);
    }

    private void handleMerchantInfoRequested(Event event) {
        var merchantId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);

        var merchant = getMerchantById(merchantId).orElse(null);

        var merchantInfoProvidedEvent = new Event(TopicNames.MERCHANT_INFO_PROVIDED, merchant, correlationId);
        queue.publish(merchantInfoProvidedEvent);
    }

    private void handlePaymentInfoRequested(Event event) {
        var paymentRequest = event.getArgument(0, PaymentRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

        try {
            var customer = getByCustomerId(paymentRequest.customerId()).orElse(null);
            var merchant = getMerchantById(paymentRequest.merchantId()).orElse(null);

            var paymentInfoProvidedEvent = new Event(TopicNames.PAYMENT_INFO_PROVIDED, customer, merchant, correlationId);
            queue.publish(paymentInfoProvidedEvent);

        } catch (IllegalArgumentException e) {
            System.err.println("handlePaymentInfoRequested: Invalid UUID format in payment request");
        }
    }

    private Optional<Merchant> getMerchantById(String id) {
        try {
            var uuid = UUID.fromString(id);
            var merchant = merchants.get(uuid);
            return Optional.ofNullable(merchant);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Customer> getByCustomerId(String id) {
        try {
            var uuid = UUID.fromString(id);
            var customer = customers.get(uuid);
            return Optional.ofNullable(customer);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }


}
