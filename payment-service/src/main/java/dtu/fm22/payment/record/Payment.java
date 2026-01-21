package dtu.fm22.payment.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @author s200718, s205135, s232268
 */
public record Payment(
        UUID id,
        Customer customer,
        Merchant merchant,
        BigDecimal amount,
        String token,
        String timestamp
) {

    public Payment withObfuscatedCustomer() {
        return new Payment(id, null, merchant, amount, token, timestamp);
    }
}
