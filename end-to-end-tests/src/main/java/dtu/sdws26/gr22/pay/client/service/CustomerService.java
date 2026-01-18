package dtu.sdws26.gr22.pay.client.service;

import dtu.sdws26.gr22.pay.client.record.Customer;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;

public class CustomerService {
    private final String baseUrl;

    public CustomerService() {
        this("http://localhost:8080/customers");
    }

    public CustomerService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Customer register(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).request().post(Entity.json(customer))) {
                return response.readEntity(Customer.class);
            }
        }
    }

    public void unregister(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            client.target(baseUrl).path(customer.id.toString()).request().delete();
        }
    }


}
