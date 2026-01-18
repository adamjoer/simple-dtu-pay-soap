package dtu.sdws26.gr22.pay.client.record;

import java.util.UUID;

public class Customer {
    public UUID id;
    public String firstName;
    public String lastName;
    public String cprNumber;
    public String bankId;

    public Customer() {
    }

    public Customer(UUID id, String firstName, String lastName, String cprNumber, String bankId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNumber = cprNumber;
        this.bankId = bankId;
    }
}
