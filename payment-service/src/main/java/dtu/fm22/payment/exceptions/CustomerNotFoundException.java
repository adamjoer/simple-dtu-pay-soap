package dtu.fm22.payment.exceptions;

public class CustomerNotFoundException extends DTUPayException {
    public CustomerNotFoundException(String id) {
        super(String.format("customer with id \"%s\" is unknown", id));
    }
}
