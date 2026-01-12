package org.acme.exceptions;

public class PaymentException extends DTUPayException {
    public PaymentException(String message) {
        super(message);
    }
}
