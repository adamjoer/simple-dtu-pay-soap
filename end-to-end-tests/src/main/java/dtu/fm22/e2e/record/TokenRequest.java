package dtu.fm22.e2e.record;

public record TokenRequest(
        String customerId,
        int numberOfTokens
) {
}
