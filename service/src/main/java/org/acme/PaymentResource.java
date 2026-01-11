package org.acme;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.record.Costumer;
import org.acme.service.PaymentService;

import java.awt.*;

@Path("/payments")
public class PaymentResource {

    private final PaymentService paymentService;

    @Inject
    public PaymentResource(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String registerCostumer(Costumer costumer) {
        var id = paymentService.registerCostumer(costumer.name());
        return id.toString();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Costumer getCostumerById(@PathParam("id") String id) {
        try {
            var uuid = java.util.UUID.fromString(id);
            return paymentService.getCostumerById(uuid).orElseThrow(() -> new NotFoundException("Costumer not found"));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid UUID format");
        }
    }
}
