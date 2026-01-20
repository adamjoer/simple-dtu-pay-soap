package dtu.fm22.facade.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull
        @NotEmpty
        String customerId,

        @NotNull
        @NotEmpty
        String merchantId,

        @NotNull
        @NotEmpty
        String amount,

        @NotNull
        @NotEmpty
        String token
) {
}
