package dtu.sdws26.gr22.pay.service.record;

public record TokenValidationRequest(
        String tokenValue,
        String customerId
) {
}
