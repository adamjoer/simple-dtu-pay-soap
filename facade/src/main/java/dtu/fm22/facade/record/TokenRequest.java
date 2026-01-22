package dtu.fm22.facade.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author s242576,s200718, s205135, s232268
 */
public record TokenRequest(
        @NotNull
        @NotEmpty
        String customerId,

        @NotNull
        int numberOfTokens
) {
}
