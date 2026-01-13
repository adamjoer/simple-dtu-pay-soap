package dtu.sdws26.gr22.pay.service.exceptions;

public class MerchantNotFoundException extends DTUPayException {
    public MerchantNotFoundException(String id) {
        super(String.format("merchant with id \"%s\" is unknown", id));
    }
}
