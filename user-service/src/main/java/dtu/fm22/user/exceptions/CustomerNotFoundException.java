package dtu.fm22.user.exceptions;

/**
 * @author s200718, s205135, s232268
 */
public class CustomerNotFoundException extends DTUPayException {
    public CustomerNotFoundException(String id) {
        super(String.format("customer with id \"%s\" is unknown", id));
    }
}
