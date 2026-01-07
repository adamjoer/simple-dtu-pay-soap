package dtu.example.service;

import dtu.example.record.Customer;
import dtu.example.record.Merchant;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;

public class SimpleDtuPay {

    private final String baseUrl;

    public SimpleDtuPay() {
        this("http://localhost:8080");
    }

    public SimpleDtuPay(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String register(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path("pay").request().post(Entity.json(customer))) {
                return response.toString();
            }
        }
    }
    public String register(Merchant customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path("pay").request().post(Entity.json(customer))) {
                return response.toString();
            }
        }
    }

    public boolean pay(Integer amount, String customerId, String merchantId) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path("pay")
                    .path(amount.toString()).path(customerId)
                    .path(merchantId).request().post(null)) {
                return true;
            }catch (Exception ex){
                throw new RuntimeException("payment failed");
            }
        }
    }
}
