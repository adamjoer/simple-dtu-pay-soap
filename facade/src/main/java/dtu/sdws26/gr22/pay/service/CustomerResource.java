package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.exceptions.DTUPayException;
import dtu.sdws26.gr22.pay.service.record.Customer;
import dtu.sdws26.gr22.pay.service.record.TokenRequest;
import dtu.sdws26.gr22.pay.service.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/customers")
public class CustomerResource {

    public final CustomerService customerService;

    @Inject
    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Customer registerCustomer(Customer customer) {
        return customerService.register(customer);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Customer getCustomerById(@PathParam("id") String id) {
        return customerService.getById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @DELETE
    @Path("/{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        try {
            customerService.unregister(id);
        } catch (DTUPayException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @GET
    @Path("/{id}/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<String> getTokens(@PathParam("id") String id) {
        try {
            return customerService.getTokens(id);
        } catch (Exception e) {
            throw new NotFoundException("Failed to get tokens: " + e.getMessage());
        }
    }

    @POST
    @Path("/{id}/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<String> requestTokens(@PathParam("id") String id, TokenRequest tokenRequest) {
        try {
            // Ensure the customerId in the request matches the path parameter
            if (!tokenRequest.customerId().equals(id)) {
                throw new BadRequestException("Customer ID in path does not match request body");
            }
            return customerService.requestTokens(id, tokenRequest.numberOfTokens());
        } catch (RuntimeException e) {
            throw new BadRequestException("Failed to request tokens: " + e.getMessage());
        }
    }
}
