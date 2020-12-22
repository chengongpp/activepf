package cn.predmet;

import java.util.Random;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

enum Role {
    PRODUCER, CONSUMER
}

public class App {
    public static void main(String[] args) {
        String brokerURL = "tcp://127.0.0.1:61616";
        String dstName = "QUEUE";
        Role role = Role.CONSUMER;
        for (int i = 0; i < args.length - 1; i++) {
            if ("brokerURL".equals(args[i])) {
                brokerURL = args[i+1];
                System.out.println("INIT Set brokerURL to " + brokerURL);
            }
        }
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(brokerURL);
        Connection connection;

        int ackMode = Session.AUTO_ACKNOWLEDGE;
        int msgSize = 10;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "useAsyncSend":
                    cf.setUseAsyncSend(true);
                    System.out.println("INIT useAsyncSend");
                    break;
                case "useOptimizeAck":
                    cf.setOptimizeAcknowledge(true);
                    cf.setOptimizeAcknowledgeTimeOut(10000);
                    System.out.println("INIT useOptimizeAck, timeout=10000");
                    break;
                case "useDupAck":
                    ackMode = Session.DUPS_OK_ACKNOWLEDGE;
                    System.out.println("INIT useDupAck");
                    break;
                case "producer":
                    role = Role.PRODUCER;
                    break;
                case "disableSessionAsync":
                    cf.setAlwaysSessionAsync(false);
                    System.out.println("INIT disableSessionAsync");
                    break;
                case "msgSize":
                    msgSize = Integer.parseInt(args[i + 1]);
                    System.out.println("INIT Message to be send: " + msgSize * 100000);
                    break;
            }
        }
        try {
            connection = cf.createConnection();
            connection.start();
            Session session = connection.createSession(false, ackMode);
            System.out.println("INIT Connected to broker " + cf.getBrokerURL());
            if (role == Role.PRODUCER) {
                for (int i = 0; i < msgSize; i++) {
                    Thread thread = new Thread(new TestProducerApp(session, dstName));
                    thread.run();
                }
            } else if (role == Role.CONSUMER) {
                Thread thread = new Thread(new TestConsumerApp(session, dstName));
                Thread thread2 = new Thread(new TestConsumerApp(session, dstName));
                Thread thread3 = new Thread(new TestConsumerApp(session, dstName));
                Thread thread4 = new Thread(new TestConsumerApp(session, dstName));
                thread.run();
                thread2.run();
                thread3.run();
                thread4.run();
            } else {
                System.out.println("ERRR You are not supposed to be here");
                return;
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

class TestProducerApp implements Runnable {

    Session session;
    Queue queue;
    MessageProducer producer;
    private static final char[] page = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final Random r = new Random();

    public TestProducerApp(Session session, String destinationNmae) {
        this.session = session;
        try {
            this.queue = session.createQueue(destinationNmae);
            this.producer = session.createProducer(this.queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            System.out.println("INIT Producer initialized");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomString() {
        char[] buf = new char[768];
        for (int i = 0; i < 768; i++) {
            int index = r.nextInt(36);
            buf[i] = page[index];
        }
        return new String(buf);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 1_000_000; i++) {
                TextMessage msg = this.session.createTextMessage(getRandomString());
                this.producer.send(this.queue, msg);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.out.println("INFO 1M messages was sent");
    }
}

class TestConsumerApp implements Runnable, MessageListener {

    Session session;
    Queue queue;
    MessageConsumer consumer;

    public TestConsumerApp(Session session, String destinationNmae) {
        this.session = session;
        try {
            this.queue = session.createQueue(destinationNmae);
            this.consumer = session.createConsumer(this.queue);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        session.close();
    }

    @Override
    public void run() {
        try {
            this.consumer.setMessageListener(this);
            System.out.println("INIT Consumer is listening");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(Message msg) {
        TextMessage tMsg = (TextMessage) msg;
        try {
            String content = tMsg.getText();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
