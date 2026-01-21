package dtu.fm22.facade.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * @author s200718, s205135, s232268
 */
public record Merchant(
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
