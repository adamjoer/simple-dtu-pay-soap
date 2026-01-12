package org.acme;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.exceptions.DTUPayException;
import org.acme.exceptions.PaymentException;
import org.acme.record.Customer;
import org.acme.record.Merchant;
import org.acme.record.PaymentRequest;
import org.acme.service.DTUPayService;

import java.math.BigDecimal;

@Path("/pay")
public class PaymentResource {

    private final DTUPayService payService;

    @Inject
    public PaymentResource(DTUPayService payService) {
        this.payService = payService;
    }

    @POST
    @Path("/customers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerCustomer(Customer customer) {
        try {
            var id = payService.registerCustomer(
                    customer.firstName(),
                    customer.lastName(),
                    customer.cprNumber(),
                    customer.bankId()
            );
            return id.toString();
        } catch (NullPointerException e) {
            throw new BadRequestException("Missing required fields");
        }
    }

    @GET
    @Path("/customers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Customer getCustomerById(@PathParam("id") String id) {
        return payService.getCustomerById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @DELETE
    @Path("/customers/{id}")
    public void deleteCustomer(@PathParam("id") String id) {
        try {
            payService.deleteCustomer(id);
        } catch (DTUPayException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @POST
    @Path("/merchants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerMerchant(Merchant merchant) {
        try {
            var id = payService.registerMerchant(
                    merchant.firstName(),
                    merchant.lastName(),
                    merchant.cprNumber(),
                    merchant.bankId()
            );
            return id.toString();
        } catch (NullPointerException e) {
            throw new BadRequestException("Missing required fields");
        }
    }

    @GET
    @Path("/merchants/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Merchant getMerchantById(@PathParam("id") String id) {
        return payService.getMerchantById(id).orElseThrow(() -> new NotFoundException("Merchant not found"));
    }

    @DELETE
    @Path("/merchants/{id}")
    public void deleteMerchant(@PathParam("id") String id) {
        try {
            payService.deleteMerchant(id);
        } catch (DTUPayException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @POST
    @Path("/payments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPayment(PaymentRequest paymentRequest) {
        try {
            var paymentId = payService.createPayment(
                    paymentRequest.customerId(),
                    paymentRequest.merchantId(),
                    new BigDecimal(paymentRequest.amount())
            );
            return Response.ok().entity(paymentId.toString()).build();
        } catch (PaymentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (DTUPayException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing required fields").build();
        }
    }

    @GET
    @Path("/payments")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAllPayments() {
        return payService.getAllPayments();
    }

    @GET
    @Path("/payments/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getPaymentById(@PathParam("id") String id) {
        return payService.getPaymentById(id).orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}
