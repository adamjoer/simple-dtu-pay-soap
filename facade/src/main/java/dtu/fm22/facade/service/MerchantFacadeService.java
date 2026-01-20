package dtu.fm22.facade.service;

import dtu.fm22.facade.record.Merchant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public final class MerchantFacadeService {

    private final Map<UUID, CompletableFuture<Merchant>> merchantsInProgress = new ConcurrentHashMap<>();

    private final MessageQueue queue;


    @Inject
    public MerchantFacadeService(MessageQueue queue) {
        this.queue = queue;
        queue.addHandler(TopicNames.MERCHANT_REGISTRATION_COMPLETED, this::handleMerchantRegistrationCompleted);
        queue.addHandler(TopicNames.MERCHANT_INFO_PROVIDED, this::handleMerchantInfoProvided);
    }

    public Merchant register(Merchant merchant) {
        var id = UUID.randomUUID();

        merchantsInProgress.put(id, new CompletableFuture<>());
        var event = new Event(TopicNames.MERCHANT_REGISTRATION_REQUESTED, merchant, id);
        queue.publish(event);

        return merchantsInProgress.get(id).orTimeout(5, TimeUnit.SECONDS).join();
    }

    public void unregister(String id) {
        var event = new Event(TopicNames.MERCHANT_UNREGISTRATION_REQUESTED, id);
        queue.publish(event);
    }

    public Optional<Merchant> getById(String id) {
        var correlationId = UUID.randomUUID();
        merchantsInProgress.put(correlationId, new CompletableFuture<>());
        var event = new Event(TopicNames.MERCHANT_INFO_REQUESTED, id, correlationId);
        queue.publish(event);

        return Optional.ofNullable(merchantsInProgress.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join());
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
