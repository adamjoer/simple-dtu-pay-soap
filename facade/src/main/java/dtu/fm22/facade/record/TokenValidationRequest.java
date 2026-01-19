package dtu.fm22.facade.record;

public record TokenValidationRequest(
        String tokenValue,
        String customerId
) {
}
