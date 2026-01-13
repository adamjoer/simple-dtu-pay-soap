package dtu.sdws26.gr22.pay.service;


import dtu.sdws26.gr22.pay.service.record.Payment;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dtu.sdws26.gr22.pay.service.exceptions.DTUPayException;
import dtu.sdws26.gr22.pay.service.exceptions.PaymentException;
import dtu.sdws26.gr22.pay.service.record.PaymentRequest;
import dtu.sdws26.gr22.pay.service.service.PaymentService;

import java.math.BigDecimal;
import java.util.Collection;

@Path("/payments")
public class PaymentResource {

    private final PaymentService payService;

    @Inject
    public PaymentResource(PaymentService payService) {
        this.payService = payService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createPayment(PaymentRequest paymentRequest) {
        try {
            var paymentId = payService.createPayment(paymentRequest);
            return Response.ok().entity(paymentId.toString()).build();
        } catch (PaymentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (DTUPayException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Payment> getAllPayments() {
        return payService.getAllPayments();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Payment getPaymentById(@PathParam("id") String id) {
        return payService.getPaymentById(id).orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}
