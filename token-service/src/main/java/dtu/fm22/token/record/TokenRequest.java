package dtu.fm22.token.record;

public record TokenRequest(
        String customerId,
        int numberOfTokens
) {
}
