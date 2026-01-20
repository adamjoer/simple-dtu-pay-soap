package dtu.fm22.e2e.service;

import dtu.fm22.e2e.record.Customer;
import dtu.fm22.e2e.record.Payment;
import dtu.fm22.e2e.record.TokenRequest;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

import java.util.Collection;
import java.util.List;

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

    public List<String> requestMoreTokens(Customer customer, int amount) {
        TokenRequest tokenRequest = new TokenRequest(customer.id.toString(), amount);
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path(tokenRequest.customerId()).path("tokens").request().post(Entity.json(tokenRequest))) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                }
                return response.readEntity(new GenericType<>() {
                });
            }
        }
    }

    public List<String> retrieveTokens(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path(customer.id.toString()).path("tokens").request().get()) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                }
                return response.readEntity(new GenericType<>() {
                });
            }
        }
    }

    public Collection<Payment> getReport(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path(customer.id.toString()).path("reports").request().get()) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                }
                return response.readEntity(new GenericType<>() {
                });
            }
        }
    }

    public Customer getProfileInformation(Customer customer) {
        try (var client = ClientBuilder.newClient()) {
            try (var response = client.target(baseUrl).path(customer.id.toString()).request().get()) {
                if (response.getStatus() != 200) {
                    throw new RuntimeException(response.readEntity(String.class));
                }
                return response.readEntity(Customer.class);
            }
        }
    }
}
