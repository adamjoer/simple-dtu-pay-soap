package dtu.fm22.e2e.service;

import dtu.fm22.e2e.record.Customer;
import dtu.fm22.e2e.record.Merchant;
import dtu.fm22.e2e.record.Payment;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

import java.util.Collection;

public class MerchantService {
    private final String baseUrl;

    public MerchantService() {
        this("http://localhost:8080/merchants");
    }

    public MerchantService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Merchant register(Merchant merchant) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).request().post(Entity.json(merchant))) {
                return response.readEntity(Merchant.class);
            }
        }
    }

    public void unregister(Merchant merchant) {
        try (var client = ClientBuilder.newClient()) {
            client.target(baseUrl).path(merchant.id.toString()).request().delete();
        }
    }

    public Collection<Payment> getReport(Merchant merchant) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path(merchant.id.toString()).path("reports").request().get()) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                }
                return response.readEntity(new GenericType<>() {
                });
            }
        }
    }

    public Merchant getProfileInformation(Merchant merchant) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path(merchant.id.toString()).request().get()) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                }
                return response.readEntity(Merchant.class);
            }
        }
    }
}
