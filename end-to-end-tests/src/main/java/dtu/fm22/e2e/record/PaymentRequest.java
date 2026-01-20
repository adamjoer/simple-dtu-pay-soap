package dtu.fm22.e2e.record;

public record PaymentRequest(
        String customerId,
        String merchantId,
        String amount,
        String token
) {
}
