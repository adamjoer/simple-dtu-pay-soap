package dtu.fm22.user;

import dtu.fm22.user.exceptions.CustomerNotFoundException;
import dtu.fm22.user.exceptions.MerchantNotFoundException;
import dtu.fm22.user.record.Customer;
import dtu.fm22.user.record.Merchant;
import dtu.fm22.user.record.PaymentInfo;
import dtu.fm22.user.record.PaymentInfoRequest;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;
import messaging.implementations.RabbitMqResponse;

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

    /**
     * @author s200718, s205135, s232268
     */
    private Customer registerCustomer(Customer customer) {
        var id = UUID.randomUUID();
        customer = customer.withId(id);
        customers.put(id, customer);
        return customer;
    }

    /**
     * @author s200718, s205135, s232268
     */
    public void handleRegisterCustomer(Event event) {
        var customer = event.getArgument(0, Customer.class);
        var correlationId = event.getArgument(1, UUID.class);
        customer = registerCustomer(customer);
        var customerRegistrationEvent = new Event(TopicNames.CUSTOMER_REGISTRATION_COMPLETED, customer, correlationId);
        queue.publish(customerRegistrationEvent);
    }

    /**
     * @author s200718, s205135, s232268
     */
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

    /**
     * @author s200718, s205135, s232268
     */
    public void handleUnregisterCustomer(Event event) {
        var id = event.getArgument(0, String.class);
        try {
            unregisterCustomer(id);
        } catch (CustomerNotFoundException e) {
            var errorResponse = new RabbitMqResponse<>(404, e.getMessage());
            var errorEvent = new Event(TopicNames.CUSTOMER_UNREGISTRATION_COMPLETED, errorResponse);
            queue.publish(errorEvent);
            return;
        }
        var customerEvent = new Event(TopicNames.CUSTOMER_UNREGISTRATION_COMPLETED, new RabbitMqResponse<>(id));
        queue.publish(customerEvent);
    }

    /**
     * @author s200718, s205135, s232268
     */
    private Merchant registerMerchant(Merchant merchant) {
        var id = UUID.randomUUID();
        merchant = merchant.withId(id);
        merchants.put(id, merchant);
        return merchant;
    }

    /**
     * @author s200718, s205135, s232268
     */
    public void handleRegisterMerchant(Event event) {
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

    /**
     * @author s200718, s205135, s232268
     */
    public void handleUnregisterMerchant(Event event) {
        var id = event.getArgument(0, String.class);
        try {
            unregisterMerchant(id);
        } catch (MerchantNotFoundException e) {
            var errorResponse = new RabbitMqResponse<>(404, e.getMessage());
            var errorEvent = new Event(TopicNames.MERCHANT_UNREGISTRATION_COMPLETED, errorResponse);
            queue.publish(errorEvent);
            return;
        }
        var merchantEvent = new Event(TopicNames.MERCHANT_UNREGISTRATION_COMPLETED, new RabbitMqResponse<>(id));
        queue.publish(merchantEvent);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public void handleCustomerInfoRequested(Event event) {
        var customerId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);

        var customer = getByCustomerId(customerId).orElse(null);

        var customerInfoProvidedEvent = new Event(TopicNames.CUSTOMER_INFO_PROVIDED, customer, correlationId);
        queue.publish(customerInfoProvidedEvent);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public void handleMerchantInfoRequested(Event event) {
        var merchantId = event.getArgument(0, String.class);
        var correlationId = event.getArgument(1, UUID.class);

        var merchant = getMerchantById(merchantId).orElse(null);

        var merchantInfoProvidedEvent = new Event(TopicNames.MERCHANT_INFO_PROVIDED, merchant, correlationId);
        queue.publish(merchantInfoProvidedEvent);
    }

    /**
     * @author s200718, s205135, s232268
     */
    public void handlePaymentInfoRequested(Event event) {
        var paymentInfoRequest = event.getArgument(0, PaymentInfoRequest.class);
        var correlationId = event.getArgument(1, UUID.class);

        var customer = getByCustomerId(paymentInfoRequest.customerId());
        if (customer.isEmpty()) {
            var errorResponse = new RabbitMqResponse<Customer>(404, "Customer not found");
            var errorEvent = new Event(TopicNames.PAYMENT_INFO_PROVIDED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        var merchant = getMerchantById(paymentInfoRequest.merchantId());
        if (merchant.isEmpty()) {
            var errorResponse = new RabbitMqResponse<Merchant>(404, "Merchant not found");
            var errorEvent = new Event(TopicNames.PAYMENT_INFO_PROVIDED, errorResponse, correlationId);
            queue.publish(errorEvent);
            return;
        }

        var response = new RabbitMqResponse<>(new PaymentInfo(customer.get(), merchant.get()));
        var paymentInfoProvidedEvent = new Event(TopicNames.PAYMENT_INFO_PROVIDED, response, correlationId);
        queue.publish(paymentInfoProvidedEvent);

    }

    /**
     * @author s200718, s205135, s232268
     */
    private Optional<Merchant> getMerchantById(String id) {
        try {
            var uuid = UUID.fromString(id);
            var merchant = merchants.get(uuid);
            return Optional.ofNullable(merchant);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * @author s200718, s205135, s232268
     */
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
