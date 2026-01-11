package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.record.Costumer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PaymentService {

    private final ConcurrentHashMap<UUID, Costumer> database = new ConcurrentHashMap<>();

    public UUID registerCostumer(String name) {
        UUID id = UUID.randomUUID();
        var costumer = new Costumer(name);
        database.put(id, costumer);
        return id;
    }

    public Optional<Costumer> getCostumerById(UUID id) {
        var costumer = database.get(id);
        return Optional.ofNullable(costumer);
    }

    public Collection<Costumer> getCostumerByName(String name) {
        var lowerCaseName = name.toLowerCase();
        return database.values().stream()
                .filter(costumer -> costumer.name().toLowerCase().equals(lowerCaseName))
                .toList();
    }
}
