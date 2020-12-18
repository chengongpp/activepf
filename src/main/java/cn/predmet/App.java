package cn.predmet;

import java.util.UUID;

import javax.jms.Connection;
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
                    msgSize = Integer.parseInt(args[i+1]);
                    System.out.println("INIT Message to be send: " + msgSize * 10000);
                    break;
                case "brokerURL":
                    brokerURL = args[i+1];
                    break;
            }
        }
        try {
            connection = cf.createConnection();
            connection.start();
            Session session = connection.createSession(false, ackMode);
            System.out.println("INIT Connected to broker " + brokerURL);
            if (role == Role.PRODUCER) {
                for (int i = 0; i < msgSize; i++) {
                    Thread thread = new Thread(new TestProducerApp(session, dstName));
                    thread.run();
                }
            } else if (role == Role.CONSUMER) {
                Thread thread = new Thread(new TestConsumerApp(session, dstName));
                thread.run();
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

    public TestProducerApp(Session session, String destinationNmae) {
        this.session = session;
        try {
            this.queue = session.createQueue(destinationNmae);
            this.producer = session.createProducer(this.queue);
            System.out.println("INFO Producer initialized");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        for (int i = 0 ; i < 10000; i++) {
            try {
                TextMessage msg = this.session.createTextMessage(UUID.randomUUID().toString());
                this.producer.send(this.queue, msg);;
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        System.out.println("INFO 10k messages was sent");
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
            System.out.println("INIT Consumer listening");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(Message msg) {
        TextMessage tMsg = (TextMessage) msg;
        try {
            String content = tMsg.getText();
            System.out.println("CSMN " + content);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
