package fr.sewatech.jms.cdi.connector;

import javax.enterprise.inject.spi.BeanManager;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
class JmsMessageReceiver implements Runnable {

    private static final Logger logger = Logger.getLogger(JmsMessageReceiver.class.getName());

    private BeanManager beanManager;
    private JMSContext connection;
    private String destinationName;

    JmsMessageReceiver(String destinationName, BeanManager beanManager) {
        this.beanManager = beanManager;
        this.destinationName = destinationName;
    }

    public void run() {
        try {
            connection = connect();
            Destination destination = InitialContext.doLookup(destinationName);
            JMSConsumer consumer = connection.createConsumer(destination);

            ExecutorService executorService = getExecutorService();

            while (true) {
                Message message = consumer.receive();
                executorService.execute(new MessageEventAsyncSender(message, destinationName));
            }

        } catch (Exception e) {
            if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                logger.log(Level.INFO, "Receiver interrupted for destination " + destinationName);
            } else {
                logger.log(Level.WARNING, "Receiver problem for destination " + destinationName, e);
            }
        }
    }

    void shutdown() {
        connection.close();
        logger.fine("JMS disconnected");
    }

    private ExecutorService getExecutorService() {
        ExecutorService executorService;
        try {
            executorService = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
        } catch (NamingException e) {
            executorService = new ThreadPoolExecutor(16, 16, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
        }
        return executorService;
    }


    private class MessageEventAsyncSender implements Runnable {
        private Message message;
        private String destinationName;

        public MessageEventAsyncSender(Message message, String destinationName) {
            this.message = message;
            this.destinationName = destinationName;
        }

        public void run() {
            beanManager.fireEvent(message, new TopicAnnotationLiteral(destinationName));
        }
    }

    private JMSContext connect() throws NamingException {
        logger.fine("Connecting to local ");
        ConnectionFactory connectionFactory = InitialContext.doLookup("java:/ConnectionFactory");
        return connectionFactory.createContext();
    }

}
