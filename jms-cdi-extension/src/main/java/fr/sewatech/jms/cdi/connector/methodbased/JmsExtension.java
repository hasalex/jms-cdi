package fr.sewatech.jms.cdi.connector.methodbased;

import fr.sewatech.jms.cdi.api.JmsDriven;
import fr.sewatech.jms.cdi.api.JmsListen;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */
public class JmsExtension implements Extension {

    private static final Logger logger = Logger.getLogger(JmsExtension.class.getName());

    private Map<String, Method> destinationMethods = new HashMap<>();

    <T>void registerDestination(@Observes @WithAnnotations(JmsDriven.class) ProcessAnnotatedType<T> processAnnotatedType) {
        logger.fine("ProcessObserverMethod");
        AnnotatedType<T> type = processAnnotatedType.getAnnotatedType();
        Method[] methods = type.getJavaClass().getDeclaredMethods();
        for (Method method : methods) {
            Annotation annotation = method.getDeclaredAnnotation(JmsListen.class);
            if (annotation != null ) {
                destinationMethods.put(((JmsListen) annotation).value(), method);
            }
        }
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation event) {
        logger.fine("AfterDeploymentValidation ...");
        JmsInitializer.destinationMethods = destinationMethods;
    }
}
