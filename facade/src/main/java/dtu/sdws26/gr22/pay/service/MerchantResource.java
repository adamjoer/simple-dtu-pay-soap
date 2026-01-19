package dtu.sdws26.gr22.pay.service;

import dtu.sdws26.gr22.pay.service.exceptions.DTUPayException;
import dtu.sdws26.gr22.pay.service.record.Merchant;
import dtu.sdws26.gr22.pay.service.service.MerchantFacadeService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/merchants")
public class MerchantResource {

    private final MerchantFacadeService merchantFacadeService;

    @Inject
    public MerchantResource(MerchantFacadeService merchantFacadeService) {
        this.merchantFacadeService = merchantFacadeService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Merchant registerMerchant(Merchant merchant) {
        return merchantFacadeService.register(merchant);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Merchant getMerchantById(@PathParam("id") String id) {
        return merchantFacadeService.getById(id).orElseThrow(() -> new NotFoundException("Merchant not found"));
    }

    @DELETE
    @Path("/{id}")
    public void deleteMerchant(@PathParam("id") String id) {
        try {
            merchantFacadeService.unregister(id);
        } catch (DTUPayException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
