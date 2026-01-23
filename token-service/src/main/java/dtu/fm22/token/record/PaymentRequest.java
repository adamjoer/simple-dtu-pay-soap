package dtu.fm22.token.record;

/**
 * @author s200718, s205135, s232268
 */
public record PaymentRequest(
        String merchantId,
        String amount,
        String token
) {
}
