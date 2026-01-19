package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.exceptions.DTUPayException;
import dtu.sdws26.gr22.pay.service.record.Customer;
import dtu.sdws26.gr22.pay.service.service.CustomerFacadeService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/customers")
public class CustomerResource {

    public final CustomerFacadeService customerFacadeService;

    @Inject
    public CustomerResource(CustomerFacadeService customerFacadeService) {
        this.customerFacadeService = customerFacadeService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Customer registerCustomer(Customer customer) {
        return customerFacadeService.register(customer);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Customer getCustomerById(@PathParam("id") String id) {
        return customerFacadeService.getById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @DELETE
    @Path("/{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        try {
            customerFacadeService.unregister(id);
        } catch (DTUPayException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
