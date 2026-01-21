package dtu.fm22.user.record;

/**
 * @author s200718, s205135, s232268
 */
public record PaymentInfoRequest(
        String customerId,
        String merchantId
) {
}
