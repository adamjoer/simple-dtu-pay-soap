package dtu.sdws26.gr22.pay.service;


import dtu.sdws26.gr22.pay.service.record.Payment;
import dtu.sdws26.gr22.pay.service.service.PaymentServiceSingleton;
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

    private final PaymentService payService = PaymentServiceSingleton.getInstance();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Payment createPayment(PaymentRequest paymentRequest) {
        return payService.createPayment(paymentRequest);
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
