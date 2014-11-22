/**
 * Copyright 2014 Sewatech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.sewatech.jms.cdi.example;

import fr.sewatech.jms.cdi.api.JmsDriven;
import fr.sewatech.jms.cdi.api.JmsListen;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexis Hassler
 */

@ApplicationScoped @JmsDriven
public class JmsMethodDrivenBean {

    private static final Logger logger = Logger.getLogger(JmsMethodDrivenBean.class.getName());
    static final String JNDI_QUESTION = "java:global/jms/swt/Question";
    static final String JNDI_QUESTION_BIS = "java:global/jms/swt/QuestionBis";

    private AtomicInteger count = new AtomicInteger();

    @JmsListen(JNDI_QUESTION)
    public void onQuestion(Message message) throws JMSException {
        logger.fine("Received : " + count.incrementAndGet());
        System.out.println("Message received " + ((TextMessage) message).getText() + " in " + this.getClass().getName() + " on Topic " + JNDI_QUESTION );
        sleep();
        logger.fine("Done : " + count.decrementAndGet());
    }

    @JmsListen(JNDI_QUESTION_BIS)
    public void onQuestionBis(Message message) {
        System.out.println("Message received " + message + " in " + this.getClass().getName() + " on Topic " + JNDI_QUESTION_BIS);
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Sleeping thread interrupted", e);
        }
    }

}
