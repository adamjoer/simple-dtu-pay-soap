package dtu.fm22.user;

import messaging.implementations.RabbitMqQueue;

public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        var queue = new RabbitMqQueue("rabbit-mq");
        new UserService(queue);
    }
}
