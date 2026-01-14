package dtu.sdws26.gr22.pay.service.service;

import messaging.implementations.RabbitMqQueue;

public class CustomerServiceSingleton {
    private static CustomerService instance = null;

    public static CustomerService getInstance() {
        if (instance != null) {
            return instance;
        }

        var queue = new RabbitMqQueue("rabbitMq");
        instance = new CustomerService(queue);
        return instance;
    }
}
