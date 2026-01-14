package dtu.sdws26.gr22.pay.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

@ApplicationScoped
public class Config {
    @Produces
    @ApplicationScoped
    public MessageQueue makeMessageQueue() {
        return new RabbitMqQueue("rabbit-mq");
    }
}
