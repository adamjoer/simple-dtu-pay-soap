package dtu.fm22.facade.record;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author s200718, s205135, s232268
 */
public record PaymentRequest(
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
