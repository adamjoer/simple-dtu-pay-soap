package org.acme.record;

import java.math.BigDecimal;

public record Costumer(String name) {
    public Costumer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
/*
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be null or negative");
        }
*/
    }
}
