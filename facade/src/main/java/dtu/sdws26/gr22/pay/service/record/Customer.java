package dtu.sdws26.gr22.pay.service.record;

import java.util.UUID;

public record Customer(
        UUID id,
        String firstName,
        String lastName,
        String cprNumber,
        String bankId
) {
}
