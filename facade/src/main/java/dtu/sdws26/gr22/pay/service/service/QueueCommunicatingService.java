package dtu.sdws26.gr22.pay.service.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public abstract class QueueCommunicatingService {

    protected final MessageQueue queue;

    public QueueCommunicatingService(MessageQueue queue) {
        this.queue = queue;
    }
}
