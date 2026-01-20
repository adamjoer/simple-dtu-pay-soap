package dtu.fm22.user.record;

public record PaymentRequest(
        String customerId,
        String merchantId,
        String amount,
        String token
) {
}
