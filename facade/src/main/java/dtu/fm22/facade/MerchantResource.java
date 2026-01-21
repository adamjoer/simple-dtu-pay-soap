package dtu.fm22.facade;

import dtu.fm22.facade.record.Merchant;
import dtu.fm22.facade.record.Payment;
import dtu.fm22.facade.service.ManagerFacadeService;
import dtu.fm22.facade.service.MerchantFacadeService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collection;

@Path("/merchants")
public class MerchantResource {

    private final MerchantFacadeService merchantFacadeService;
    private final ManagerFacadeService managerFacadeService;

    @Inject
    public MerchantResource(MerchantFacadeService merchantFacadeService, ManagerFacadeService managerFacadeService) {
        this.merchantFacadeService = merchantFacadeService;
        this.managerFacadeService = managerFacadeService;
    }

    /**
     * @author s200718, s205135, s232268
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Merchant registerMerchant(@Valid Merchant merchant) {
        return merchantFacadeService.register(merchant);
    }

    /**
     * @author s200718, s205135, s232268
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Merchant getMerchantById(@PathParam("id") String id) {
        return merchantFacadeService.getById(id).orElseThrow(() -> new NotFoundException("Merchant not found"));
    }

    /**
     * @author s200718, s205135, s232268
     */
    @DELETE
    @Path("/{id}")
    public void deleteMerchant(@PathParam("id") String id) {
        merchantFacadeService.unregister(id);
    }

    /**
     * @author s200718, s205135, s232268
     */
    @GET
    @Path("/{id}/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Payment> getReport(@PathParam("id") String id) {
        return managerFacadeService.getMerchantReport(id).orElseThrow(() -> new NotFoundException("Merchant not found"));
    }
}
