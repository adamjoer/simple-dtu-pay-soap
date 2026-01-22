package dtu.fm22.token;

import messaging.implementations.RabbitMqQueue;
/*
 ** author s242576
 */
public class StartUp {
    public static void main(String[] args) throws Exception {
        new StartUp().startUp();
    }

    private void startUp() throws Exception {
        var queue = new RabbitMqQueue("rabbit-mq");
        new TokenService(queue);
    }
}
