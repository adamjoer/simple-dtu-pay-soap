package dtu.fm22.facade.service;

import com.google.gson.reflect.TypeToken;
import dtu.fm22.facade.record.Payment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import messaging.Event;
import messaging.MessageQueue;
import messaging.TopicNames;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ManagerFacadeService {

    private final MessageQueue queue;
    private final CustomerFacadeService customerFacadeService;
    private final MerchantFacadeService merchantFacadeService;

    private final Map<UUID, CompletableFuture<Collection<Payment>>> pendingReports = new ConcurrentHashMap<>();

    @Inject
    public ManagerFacadeService(
            MessageQueue queue,
            MerchantFacadeService merchantFacadeService,
            CustomerFacadeService customerFacadeService) {
        this.queue = queue;
        this.merchantFacadeService = merchantFacadeService;
        this.customerFacadeService = customerFacadeService;

        this.queue.addHandler(TopicNames.CUSTOMER_REPORT_PROVIDED, this::handleReportProvided);
        this.queue.addHandler(TopicNames.MERCHANT_REPORT_PROVIDED, this::handleReportProvided);
        this.queue.addHandler(TopicNames.MANAGER_REPORT_PROVIDED, this::handleReportProvided);
    }

    public Optional<Collection<Payment>> getCustomerReport(String id) {
        var maybeCustomer = customerFacadeService.getById(id);
        if (maybeCustomer.isEmpty()) {
            return Optional.empty();
        }
        var customer = maybeCustomer.get();

        pendingReports.put(customer.id(), new CompletableFuture<>());
        var event = new Event(TopicNames.CUSTOMER_REPORT_REQUESTED, customer, customer.id());
        queue.publish(event);

        return Optional.ofNullable(pendingReports.get(customer.id()).orTimeout(5, TimeUnit.SECONDS).join());
    }

    public Optional<Collection<Payment>> getMerchantReport(String id) {
        var maybeMerchant = merchantFacadeService.getById(id);
        if (maybeMerchant.isEmpty()) {
            return Optional.empty();
        }
        var merchant = maybeMerchant.get();

        pendingReports.put(merchant.id(), new CompletableFuture<>());
        var event = new Event(TopicNames.MERCHANT_REPORT_REQUESTED, merchant, merchant.id());
        queue.publish(event);

        return Optional.ofNullable(pendingReports.get(merchant.id()).orTimeout(5, TimeUnit.SECONDS).join());
    }

    public Collection<Payment> getManagerReport() {
        var correlationId = UUID.randomUUID();
        pendingReports.put(correlationId, new CompletableFuture<>());
        var event = new Event(TopicNames.MANAGER_REPORT_REQUESTED, correlationId);
        queue.publish(event);

        return pendingReports.get(correlationId).orTimeout(5, TimeUnit.SECONDS).join();
    }

    public void handleReportProvided(Event event) {
        var collectionType = new TypeToken<Collection<Payment>>() {
        };
        var report = event.getArgument(0, collectionType);
        var correlationId = event.getArgument(1, UUID.class);

        var future = pendingReports.get(correlationId);
        if (future != null) {
            future.complete(report);
        }
    }
}
