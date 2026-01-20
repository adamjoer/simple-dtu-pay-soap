package dtu.fm22.token.record;

import java.util.UUID;

public record Token(
        String tokenValue,
        UUID customerId,
        boolean used
) {
    public Token withUsed(boolean used) {
        return new Token(tokenValue, customerId, used);
    }
}
