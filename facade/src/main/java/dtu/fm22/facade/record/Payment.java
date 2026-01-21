package dtu.fm22.facade.record;

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
        String timestamp,
        String token
) {
}
