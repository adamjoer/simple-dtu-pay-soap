package dtu.sdws26.gr22.pay.client.service;

import dtu.sdws26.gr22.pay.client.record.Merchant;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;

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

}
