package fr.sewatech.jms.cdi.connector;

import fr.sewatech.jms.cdi.api.JmsDestination;

import javax.enterprise.util.AnnotationLiteral;

class JmsDestinationAnnotationLiteral extends AnnotationLiteral<JmsDestination> implements JmsDestination {
    private String value;

    JmsDestinationAnnotationLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

}
