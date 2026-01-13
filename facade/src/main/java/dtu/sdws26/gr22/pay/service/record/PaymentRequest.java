package dtu.sdws26.gr22.pay.service.record;

public record PaymentRequest(
        String customerId,
        String merchantId,
        String amount
) {
}
