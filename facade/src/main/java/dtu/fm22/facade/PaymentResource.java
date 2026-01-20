package dtu.fm22.facade;

import dtu.fm22.facade.record.PaymentRequest;
import dtu.fm22.facade.service.PaymentFacadeService;
import dtu.fm22.facade.record.Payment;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collection;

@Path("/payments")
public class PaymentResource {

    private final PaymentFacadeService payService;

    @Inject
    public PaymentResource(PaymentFacadeService payService) {
        this.payService = payService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Payment createPayment(@Valid PaymentRequest paymentRequest) {
        return payService.createPayment(paymentRequest);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Payment getPaymentById(@PathParam("id") String id) {
        return payService.getPaymentById(id).orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}
