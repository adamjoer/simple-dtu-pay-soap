package dtu.fm22.payment.record;

public record PaymentRequest(
        String customerId,
        String merchantId,
        String amount,
        String token
) {
}
