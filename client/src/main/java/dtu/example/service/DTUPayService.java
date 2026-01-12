package dtu.example.service;

import dtu.example.record.Customer;
import dtu.example.record.Merchant;
import dtu.example.record.Payment;
import dtu.example.record.PaymentRequest;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

import java.util.Collection;

public class DTUPayService {

    private final String baseUrl;

    public DTUPayService() {
        this("http://localhost:8080");
    }

    public DTUPayService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String register(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path("customers").request().post(Entity.json(customer))) {
                return response.readEntity(String.class);
            }
        }
    }

    public void unregisterCustomer(String customerId) {
        try (var client = ClientBuilder.newClient()) {
            client.target(baseUrl).path("customers").path(customerId).request().delete();
        }
    }

    public String register(Merchant merchant) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path("merchants").request().post(Entity.json(merchant))) {
                return response.readEntity(String.class);
            }
        }
    }

    public void unregisterMerchant(String merchantId) {
        try (var client = ClientBuilder.newClient()) {
            client.target(baseUrl).path("merchants").path(merchantId).request().delete();
        }
    }

    public boolean pay(String amount, String customerId, String merchantId) {
        try (var client = ClientBuilder.newClient()) {
            var paymentRequest = new PaymentRequest(customerId, merchantId, amount);
            try (var response = client.target(baseUrl).path("payments").request().post(Entity.json(paymentRequest))) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                } else {
                    return true;
                }
            }
        }
    }

    public Collection<Payment> getAllPayments() {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path("payments").request().get()) {
                return response.readEntity(new GenericType<>() {
                });
            }
        }
    }
}
