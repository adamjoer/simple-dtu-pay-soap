package dtu.fm22.user.record;

import java.util.UUID;

/**
 * @author s200718, s205135, s232268
 */
public record Merchant(
        UUID id,
        String firstName,
        String lastName,
        String cprNumber,
        String bankId
) {
    public Merchant withId(UUID id) {
        return new Merchant(id, firstName, lastName, cprNumber, bankId);
    }
}
