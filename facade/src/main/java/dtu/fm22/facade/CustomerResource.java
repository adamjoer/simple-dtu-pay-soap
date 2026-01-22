package dtu.fm22.facade;

import dtu.fm22.facade.record.Customer;
import dtu.fm22.facade.record.Payment;
import dtu.fm22.facade.record.TokenRequest;
import dtu.fm22.facade.service.CustomerFacadeService;
import dtu.fm22.facade.service.ManagerFacadeService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collection;

@Path("/customers")
public class CustomerResource {

    public final CustomerFacadeService customerFacadeService;
    public final ManagerFacadeService managerFacadeService;

    @Inject
    public CustomerResource(CustomerFacadeService customerFacadeService, ManagerFacadeService managerFacadeService) {
        this.customerFacadeService = customerFacadeService;
        this.managerFacadeService = managerFacadeService;
    }

    /**
     * @author s200718, s205135, s232268
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Customer registerCustomer(@Valid Customer customer) {
        return customerFacadeService.register(customer);
    }

    /**
     * @author s200718, s205135, s232268
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Customer getCustomerById(@PathParam("id") String id) {
        return customerFacadeService.getById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    /**
     * @author s200718, s205135, s232268
     */
    @DELETE
    @Path("/{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        customerFacadeService.unregister(id);
    }

    @GET
    @Path("/{id}/tokens")
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<String> getTokens(@PathParam("id") String id) {
        try {
            return customerFacadeService.getTokens(id);
        } catch (Exception e) {
            throw new NotFoundException("Failed to get tokens: " + e.getMessage());
        }
    }

    @POST
    @Path("/{id}/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<String> requestTokens(@PathParam("id") String id, @Valid TokenRequest tokenRequest) {
        // Ensure the customerId in the request matches the path parameter
        if (!tokenRequest.customerId().equals(id)) {
            throw new BadRequestException("Customer ID in path does not match request body");
        }
        return customerFacadeService.requestTokens(id, tokenRequest.numberOfTokens());
    }

    /**
     * @author s200718, s205135, s232268
     */
    @GET
    @Path("/{id}/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Payment> getReport(@PathParam("id") String id) {
        return managerFacadeService.getCustomerReport(id).orElseThrow(() -> new NotFoundException("Customer not " +
                "found"));
    }
}
