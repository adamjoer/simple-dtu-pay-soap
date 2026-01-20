package dtu.fm22.facade.exceptions;

public class MerchantNotFoundException extends DTUPayException {
    public MerchantNotFoundException(String id) {
        super(String.format("merchant with id \"%s\" is unknown", id));
    }
}
