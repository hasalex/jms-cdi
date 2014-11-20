package fr.sewatech.jms.cdi.connector;

import fr.sewatech.jms.cdi.api.JmsInboundTopic;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.jms.Message;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
public class JmsExtension implements Extension {

    private static final Logger logger = Logger.getLogger(JmsExtension.class.getName());

    private Set<String> destinationNames = new HashSet<>();
    private List<JmsMessageReceiver> receivers = new ArrayList<>();

    void registerTopic(@Observes ProcessObserverMethod<Message, ?> observerMethod) {
        logger.fine("ProcessObserverMethod");
        Set<Annotation> qualifiers = observerMethod.getObserverMethod().getObservedQualifiers();
        for (Annotation qualifier : qualifiers) {
            if (qualifier instanceof JmsInboundTopic) {
                destinationNames.add(((JmsInboundTopic) qualifier).value());
            }
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        logger.fine("AfterDeploymentValidation ...");
        for (String destinationName : destinationNames) {
            JmsMessageReceiver receiver = new JmsMessageReceiver(destinationName, beanManager);
            receivers.add(receiver);
            newThread(receiver).start();
        }
    }

    void shutdown(@Observes BeforeShutdown beforeShutdown) {
        logger.fine("Before shutdown ...");
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
