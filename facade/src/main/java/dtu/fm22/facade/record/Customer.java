package dtu.fm22.facade.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record Customer(
        UUID id,
        @NotNull
        @NotEmpty
        String firstName,

        @NotNull
        @NotEmpty
        String lastName,

        @NotNull
        @NotEmpty
        String cprNumber,

        @NotNull
        @NotEmpty
        String bankId
) {
}
