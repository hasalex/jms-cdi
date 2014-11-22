package fr.sewatech.jms.cdi.example;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexis Hassler
 */
@JMSDestinationDefinitions({
        @JMSDestinationDefinition(
                name = JmsObserverBean.JNDI_QUESTION,
                interfaceName = "javax.jms.Topic")
        ,
        @JMSDestinationDefinition(
                name = JmsObserverBean.JNDI_QUESTION_BIS,
                interfaceName = "javax.jms.Topic")

})
@WebServlet(name = "SendServlet", urlPatterns = "/")
public class PublishServlet extends HttpServlet {

    @Inject
    JMSContext jmsContext;

    @Resource(mappedName=JmsObserverBean.JNDI_QUESTION)
    Destination destination;

    @Inject
    private fr.sewatech.jms.cdi.connector.eventbased.JmsInitializer jmsEventBasedInitializer;

    @Inject
    private fr.sewatech.jms.cdi.connector.methodbased.JmsInitializer jmsMethodBasedInitializer;

    public void init() {
        jmsEventBasedInitializer.init();
        jmsMethodBasedInitializer.init();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        sendMessageAndForward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        sendMessageAndForward(request, response);
    }

    private void sendMessageAndForward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String text = request.getParameter("text");
        String message = null;
        if (text != null && !text.isEmpty()) {
            send(text);
            message = "Message sent : " + text;
        }

        if (request.getHeader("Accept").contains("text/html")) {
            request.setAttribute("message", message);
            request.getRequestDispatcher("send.jsp").forward(request, response);
        } else {
            response.getWriter().println(message == null ? "Nothing sent" : message);
        }
    }

    private void send(String text) {
        jmsContext.createProducer().send(destination, text);
    }

}
