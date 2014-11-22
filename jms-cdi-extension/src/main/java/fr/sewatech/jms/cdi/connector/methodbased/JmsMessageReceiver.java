package fr.sewatech.jms.cdi.connector.methodbased;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
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
    private final String destinationName;
    private final Method method;

    JmsMessageReceiver(String destinationName, Method method) {
        this.destinationName = destinationName;
        this.method = method;
    }

    public void run() {
        try {
            jmsContext = connect();
            Destination destination = InitialContext.doLookup(destinationName);
            JMSConsumer consumer = jmsContext.createConsumer(destination);

            ExecutorService executorService = getExecutorService();

            while (true) {
                Message message = consumer.receive();
                executorService.execute(new MessageMethodAsyncCaller(message));
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

    private JMSContext connect() throws NamingException {
        logger.fine("Connecting to local ");
        ConnectionFactory connectionFactory = InitialContext.doLookup("java:comp/DefaultJMSConnectionFactory");
        return connectionFactory.createContext();
    }


    private class MessageMethodAsyncCaller implements Runnable {
        private Message message;

        public MessageMethodAsyncCaller(Message message) {
            this.message = message;
        }

        public void run() {
            Instance<?> instance = CDI.current().select(method.getDeclaringClass(), new JmsListenAnnotationLiteral());
            try {
                method.invoke(instance.get(), message);
            } catch (Exception  e) {
                throw new RuntimeException(e);
            }
        }
    }

}
