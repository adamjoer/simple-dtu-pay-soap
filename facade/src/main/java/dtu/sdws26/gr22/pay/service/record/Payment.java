package dtu.sdws26.gr22.pay.service.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payment(
        UUID id,
        Customer customer,
        Merchant merchant,
        BigDecimal amount,
        Instant timestamp
) {
}
