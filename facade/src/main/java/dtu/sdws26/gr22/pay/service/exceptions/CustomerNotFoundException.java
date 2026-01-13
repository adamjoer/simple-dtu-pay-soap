package dtu.sdws26.gr22.pay.service.exceptions;

public class CustomerNotFoundException extends DTUPayException {
    public CustomerNotFoundException(String id) {
        super(String.format("customer with id \"%s\" is unknown", id));
    }
}
