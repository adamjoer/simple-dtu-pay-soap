package dtu.sdws26.gr22.pay.client.record;

public record PaymentRequest(
        String customerId,
        String merchantId,
        String amount,
        String token
) {
}
