package dtu.sdws26.gr22.pay.service.service;

import dtu.sdws26.gr22.pay.service.record.Merchant;
import jakarta.enterprise.context.ApplicationScoped;
import messaging.Event;
import messaging.MessageQueue;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MerchantService {
    public static final String MERCHANT_REGISTRATION_REQUESTED = "MerchantRegistrationRequested";
    public static final String MERCHANT_REGISTRATION_COMPLETED = "MerchantRegistrationCompleted";

    public static final String MERCHANT_UNREGISTRATION_REQUESTED = "MerchantUnregistrationRequested";

    public static final String MERCHANT_INFO_REQUESTED = "MerchantInfoRequested";
    public static final String MERCHANT_INFO_PROVIDED = "MerchantInfoProvided";

    private final Map<UUID, CompletableFuture<Merchant>> merchantsInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;

    public MerchantService(MessageQueue queue) {
        this.queue = queue;
        this.queue.addHandler(MERCHANT_REGISTRATION_COMPLETED, this::handleMerchantRegistrationCompleted);
        this.queue.addHandler(MERCHANT_INFO_PROVIDED, this::handleMerchantInfoProvided);
    }

    public Merchant register(Merchant merchant) {
        var id = UUID.randomUUID();

        merchantsInProgress.put(id, new CompletableFuture<>());
        var event = new Event(MERCHANT_REGISTRATION_REQUESTED, merchant, id);
        queue.publish(event);

        return merchantsInProgress.get(id).join();
    }

    public void unregister(String id) {
        var event = new Event(MERCHANT_UNREGISTRATION_REQUESTED, id);
        queue.publish(event);
    }

    public Optional<Merchant> getById(String id) {
        var correlationId = UUID.randomUUID();
        merchantsInProgress.put(correlationId, new CompletableFuture<>());
        var event = new Event(MERCHANT_INFO_REQUESTED, id, correlationId);
        queue.publish(event);

        return Optional.ofNullable(merchantsInProgress.get(correlationId).join());
    }

    private void handleMerchantRegistrationCompleted(Event event) {
        var merchant = event.getArgument(0, Merchant.class);
        var correlationId = event.getArgument(1, UUID.class);

        merchantsInProgress.get(correlationId).complete(merchant);
    }

    private void handleMerchantInfoProvided(Event event) {
        var merchant = event.getArgument(0, Merchant.class);
        var correlationId = event.getArgument(1, UUID.class);

        merchantsInProgress.get(correlationId).complete(merchant);
    }
}
