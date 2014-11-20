package fr.sewatech.jms.cdi.connector;

import fr.sewatech.jms.cdi.api.JmsDestination;

import javax.enterprise.util.AnnotationLiteral;

class TopicAnnotationLiteral extends AnnotationLiteral<JmsDestination> implements JmsDestination {
    private String value;

    TopicAnnotationLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

}
