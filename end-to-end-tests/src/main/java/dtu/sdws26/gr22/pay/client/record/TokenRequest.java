package dtu.sdws26.gr22.pay.client.record;

public record TokenRequest(
        String customerId,
        int numberOfTokens
) {
}
