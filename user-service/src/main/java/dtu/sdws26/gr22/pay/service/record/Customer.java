package dtu.sdws26.gr22.pay.service.record;

import java.util.UUID;

public record Customer(
        UUID id,
        String firstName,
        String lastName,
        String cprNumber,
        String bankId
) {
    public Customer withId(UUID id) {
        return new Customer(id, firstName, lastName, cprNumber, bankId);
    }
}
