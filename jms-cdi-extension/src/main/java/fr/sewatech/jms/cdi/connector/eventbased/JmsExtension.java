package fr.sewatech.jms.cdi.connector.eventbased;

import fr.sewatech.jms.cdi.api.JmsDestination;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.jms.Message;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
public class JmsExtension implements Extension {

    private static final Logger logger = Logger.getLogger(JmsExtension.class.getName());

    private Set<String> destinationNames = new HashSet<>();

    void registerDestination(@Observes ProcessObserverMethod<Message, ?> event) {
        logger.fine("ProcessObserverMethod");
        Set<Annotation> qualifiers = event.getObserverMethod().getObservedQualifiers();
        for (Annotation qualifier : qualifiers) {
            if (qualifier instanceof JmsDestination) {
                destinationNames.add(((JmsDestination) qualifier).value());
            }
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation event) {
        logger.fine("AfterDeploymentValidation ...");
        JmsInitializer.destinationNames = destinationNames;
    }
}
