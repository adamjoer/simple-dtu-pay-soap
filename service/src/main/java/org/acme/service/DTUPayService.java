package org.acme.service;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.exceptions.CustomerNotFoundException;
import org.acme.exceptions.MerchantNotFoundException;
import org.acme.exceptions.PaymentException;
import org.acme.record.Customer;
import org.acme.record.Merchant;
import org.acme.record.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DTUPayService {
    private final String API_KEY = System.getenv("SIMPLE_DTU_PAY_API_KEY");

    private final ConcurrentHashMap<UUID, Customer> customers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Merchant> merchants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Payment> payments = new ConcurrentHashMap<>();

    private final BankService bankService = new BankService_Service().getBankServicePort();

    public UUID registerCustomer(String firstName, String lastName, String cprNumber, String bankId) {
        var id = UUID.randomUUID();
        var customer = new Customer(id, firstName, lastName, cprNumber, bankId);
        customers.put(id, customer);
        return id;
    }

    public void deleteCustomer(String id) {
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

    public Optional<Customer> getCustomerById(String id) {
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

    public void deleteMerchant(String id) {
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
        var merchant = merchants.get(id);
        return Optional.ofNullable(merchant);
    }

    public UUID createPayment(String customerId, String merchantId, BigDecimal amount) {
        UUID customerUuid;
        UUID merchantUuid;
        try {
            customerUuid = UUID.fromString(customerId);
        } catch (IllegalArgumentException e) {
            throw new CustomerNotFoundException(customerId);
        }

        try {
            merchantUuid = UUID.fromString(merchantId);
        } catch (IllegalArgumentException e) {
            throw new MerchantNotFoundException(merchantId);
        }

        var customer = customers.get(customerUuid);
        if (customer == null) {
            throw new CustomerNotFoundException(customerUuid.toString());
        }
        var merchant = merchants.get(merchantUuid);
        if (merchant == null) {
            throw new MerchantNotFoundException(merchantUuid.toString());
        }

        var paymentId = UUID.randomUUID();
        var payment = new Payment(paymentId, customer, merchant, amount, Instant.now());
        payments.put(paymentId, payment);
        try {
            bankService.transferMoneyFromTo(customer.bankId(),
                    merchant.bankId(),
                    amount,
                    "from " + customer.firstName() + " to " + merchant.firstName());
            System.console().printf("Transferred %s from %s to %s%n", amount, customer.firstName(), merchant.firstName());
        } catch (BankServiceException_Exception e) {
            throw new PaymentException("Payment failed: " + e.getMessage());
        }
        return paymentId;
    }

    public Optional<Payment> getPaymentById(String id) {
        try {
            var uuid = UUID.fromString(id);
            var payment = payments.get(uuid);
            return Optional.ofNullable(payment);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Collection<Payment> getAllPayments() {
        return payments.values();
    }
}
