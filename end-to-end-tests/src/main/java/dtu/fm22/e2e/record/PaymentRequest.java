package dtu.fm22.e2e.record;

public record PaymentRequest(
        String merchantId,
        String amount,
        String token
) {
}
