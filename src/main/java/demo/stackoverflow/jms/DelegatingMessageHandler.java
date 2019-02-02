package demo.stackoverflow.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;

/**
 * This message listener verifies that an MDB can successfully function with an injected EJB.
 *
 * We don't seem to need access to the jboss ejb3 implementation class here that is missing for the
 * {@link ContextualMessageHandler}.
 *
 * @author sfcoy
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/queues/demoQueue"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "source = 'ping2'")
})
public class DelegatingMessageHandler implements MessageListener {

    @Inject
    private Logger logger;

    @EJB
    private Delegate delegate;

    @Override
    public void onMessage(Message message) {
        try {
            logger.info("Handling a {} message", message.getClass());
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                delegate.handleMessage(textMessage.getText());
            }
        } catch (JMSException e) {
            logger.error("Failed to process message", e);
        }
    }
    
}
