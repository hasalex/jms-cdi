package fr.sewatech.jms.cdi.connector;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
@ApplicationScoped
public class JmsInitializer {

    private static final Logger logger = Logger.getLogger(JmsInitializer.class.getName());
    static Set<String> destinationNames;

    private List<JmsMessageReceiver> receivers = new ArrayList<>();

    public void init() {
        logger.fine("PostConstruct ...");
        for (String destinationName : JmsInitializer.destinationNames) {
            JmsMessageReceiver receiver = new JmsMessageReceiver(destinationName);
            receivers.add(receiver);
            newThread(receiver).start();
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.fine("PreDestroy ...");
        for (JmsMessageReceiver receiver : receivers) {
            receiver.shutdown();
        }
    }

    private Thread newThread(JmsMessageReceiver runnable) {
        ThreadFactory threadFactory;
        try {
            threadFactory = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
        } catch (NamingException e) {
            threadFactory = Executors.defaultThreadFactory();
        }
        return threadFactory.newThread(runnable);
    }


}
