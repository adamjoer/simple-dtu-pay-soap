package dtu.sdws26.gr22.pay.service.service;

import messaging.implementations.RabbitMqQueue;

public class PaymentServiceSingleton {
    private static PaymentService instance = null;

    public static PaymentService getInstance() {
        if (instance != null) {
            return instance;
        }

        var queue = new RabbitMqQueue("rabbitMq");
        instance = new PaymentService(queue);
        return instance;
    }
}
