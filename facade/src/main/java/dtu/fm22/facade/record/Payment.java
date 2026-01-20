package dtu.fm22.facade.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        Customer customer,
        Merchant merchant,
        BigDecimal amount,
        String timestamp,
        String token
) {
}
