package fr.sewatech.jms.cdi.connector.eventbased;

import javax.enterprise.inject.spi.CDI;
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

    private JMSContext jmsContext;
    private String destinationName;

    JmsMessageReceiver(String destinationName) {
        this.destinationName = destinationName;
    }

    public void run() {
        try {
            jmsContext = connect();
            Destination destination = InitialContext.doLookup(destinationName);
            JMSConsumer consumer = jmsContext.createConsumer(destination);

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
        jmsContext.close();
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
            CDI.current().getBeanManager().fireEvent(message, new JmsDestinationAnnotationLiteral(destinationName));
        }
    }

    private JMSContext connect() throws NamingException {
        logger.fine("Connecting to local ");
        ConnectionFactory connectionFactory = InitialContext.doLookup("java:comp/DefaultJMSConnectionFactory");
        return connectionFactory.createContext();
    }

}
