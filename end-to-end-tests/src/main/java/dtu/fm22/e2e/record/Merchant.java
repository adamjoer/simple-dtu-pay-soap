package dtu.fm22.e2e.record;

import java.util.UUID;

public class Merchant {
    public UUID id;
    public String firstName;
    public String lastName;
    public String cprNumber;
    public String bankId;

    public Merchant() {
    }

    public Merchant(UUID id, String firstName, String lastName, String cprNumber, String bankId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cprNumber = cprNumber;
        this.bankId = bankId;
    }

    @Override
    public String toString() {
        return "Merchant{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", cprNumber='" + cprNumber + '\'' +
                ", bankId='" + bankId + '\'' +
                '}';
    }
}
