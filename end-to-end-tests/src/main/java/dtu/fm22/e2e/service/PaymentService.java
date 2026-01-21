package dtu.fm22.e2e.service;

import dtu.fm22.e2e.record.Payment;
import dtu.fm22.e2e.record.PaymentRequest;
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

    public boolean pay(String amount, UUID merchantId, String token) {
        try (var client = ClientBuilder.newClient()) {
            var paymentRequest = new PaymentRequest(merchantId.toString(), amount, token);
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
