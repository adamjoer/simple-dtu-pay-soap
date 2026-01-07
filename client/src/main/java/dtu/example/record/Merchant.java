package dtu.example.record;

import java.util.UUID;

public class Merchant {
    public String Name;
    public UUID Uuid;

    public Merchant() {
    }

    public Merchant(String name) {
        this.Name = name;
        this.Uuid = UUID.randomUUID();
    }

}
