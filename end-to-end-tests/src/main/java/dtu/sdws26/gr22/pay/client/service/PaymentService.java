package dtu.sdws26.gr22.pay.client.service;

import dtu.sdws26.gr22.pay.client.record.Customer;
import dtu.sdws26.gr22.pay.client.record.Merchant;
import dtu.sdws26.gr22.pay.client.record.Payment;
import dtu.sdws26.gr22.pay.client.record.PaymentRequest;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

import java.util.Collection;
import java.util.UUID;

public class PaymentService {

    private final String baseUrl;

    public PaymentService() {
        this("http://localhost:8080");
    }

    public PaymentService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean pay(String amount, UUID customerId, UUID merchantId) {
        try (var client = ClientBuilder.newClient()) {
            var paymentRequest = new PaymentRequest(customerId.toString(), merchantId.toString(), amount);
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
