package dtu.sdws26.gr22.pay.service.record;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record Merchant(
        UUID id,
        @NotNull String firstName,
        @NotNull String lastName,
        @NotNull String cprNumber,
        @NotNull String bankId
) {
}
