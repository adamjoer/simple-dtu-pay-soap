package dtu.fm22.user.record;

import java.util.UUID;

/**
 * @author s200718, s205135, s232268
 */
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
