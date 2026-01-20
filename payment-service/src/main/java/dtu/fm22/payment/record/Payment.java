package dtu.fm22.payment.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
