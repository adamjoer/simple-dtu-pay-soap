package dtu.sdws26.gr22.pay.service.service;

import messaging.implementations.RabbitMqQueue;

public class MerchantServiceSingleton {
    private static MerchantService instance = null;

    public static MerchantService getInstance() {
        if (instance != null) {
            return instance;
        }

        var queue = new RabbitMqQueue("rabbitMq");
        instance = new MerchantService(queue);
        return instance;
    }
}
