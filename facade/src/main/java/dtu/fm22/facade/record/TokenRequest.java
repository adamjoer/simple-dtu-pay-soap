package dtu.fm22.facade.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TokenRequest(
        @NotNull
        @NotEmpty
        String customerId,

        @NotNull
        int numberOfTokens
) {
}
