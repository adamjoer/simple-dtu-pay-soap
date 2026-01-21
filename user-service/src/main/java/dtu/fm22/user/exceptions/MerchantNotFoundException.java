package dtu.fm22.user.exceptions;

/**
 * @author s200718, s205135, s232268
 */
public class MerchantNotFoundException extends DTUPayException {
    public MerchantNotFoundException(String id) {
        super(String.format("merchant with id \"%s\" is unknown", id));
    }
}
