package dtu.sdws26.gr22.pay.service.service;

import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

public abstract class QueueCommunicatingService {

    protected final MessageQueue queue;

    public QueueCommunicatingService(MessageQueue queue) {
        this.queue = queue;
    }
}
