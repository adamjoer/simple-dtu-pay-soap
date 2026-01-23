package dtu.fm22.facade;

import dtu.fm22.facade.record.Payment;
import dtu.fm22.facade.service.ManagerFacadeService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collection;

/**
 * @author s242575
 */
@Path("/reports")
public class ManagerResource {

    public final ManagerFacadeService managerFacadeService;

    public ManagerResource(ManagerFacadeService managerFacadeService) {
        this.managerFacadeService = managerFacadeService;
    }

    /**
     * @author s200718, s205135
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Payment> getManagerReport() {
        return managerFacadeService.getManagerReport();
    }
}
