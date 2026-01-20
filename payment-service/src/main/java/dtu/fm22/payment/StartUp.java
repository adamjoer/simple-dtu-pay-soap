package dtu.fm22.payment;

import messaging.implementations.RabbitMqQueue;

public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        var queue = new RabbitMqQueue("rabbit-mq");
        new PaymentService(queue);
    }
}
