package dtu.fm22.facade;

import dtu.fm22.facade.record.Payment;
import dtu.fm22.facade.service.ManagerFacadeService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Collection;

@Path("/reports")
public class ManagerResource {

    public final ManagerFacadeService managerFacadeService;

    public ManagerResource(ManagerFacadeService managerFacadeService) {
        this.managerFacadeService = managerFacadeService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Payment> getManagerReport() {
        return managerFacadeService.getManagerReport();
    }
}
