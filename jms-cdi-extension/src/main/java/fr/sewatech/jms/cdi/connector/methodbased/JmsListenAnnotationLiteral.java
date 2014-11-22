package fr.sewatech.jms.cdi.connector.methodbased;

import fr.sewatech.jms.cdi.api.JmsDriven;
import fr.sewatech.jms.cdi.api.JmsListen;

import javax.enterprise.util.AnnotationLiteral;

class JmsListenAnnotationLiteral extends AnnotationLiteral<JmsDriven> implements JmsDriven {

}
