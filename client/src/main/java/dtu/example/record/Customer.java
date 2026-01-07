package dtu.example.record;

import java.util.UUID;

public class Customer {

    public String Name;
    public UUID Uuid;

    public Customer() {
    }

    public Customer(String name) {
        this.Name = name;
        this.Uuid = UUID.randomUUID();
    }
}
