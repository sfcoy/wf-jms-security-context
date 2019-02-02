package demo.stackoverflow.jms;

import java.io.Serializable;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;

import demo.stackoverflow.payload.ActivePayload;

/**
 * This message listener verifies that an MDB can successfully function when a contextual object has been
 * delivered to it, as described in
 * {@link javax.enterprise.concurrent.ContextService#createContextualProxy(java.lang.Object, java.util.Map, java.lang.Class)}.
 *
 * @author sfcoy
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/queues/demoQueue"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "source = 'ping'")
})
public class ContextualMessageHandler implements MessageListener {

    @Inject
    private Logger logger;

    @Override
    public void onMessage(Message message) {
        try {
            logger.info("Handling a {} message", message.getClass());
            if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Serializable payload = objectMessage.getObject();
                if (payload instanceof ActivePayload) {
                    ActivePayload activePayload = (ActivePayload) (payload);
                    activePayload.run();
                } else {
                    logger.info("payload was not an ActivePayload");
                }
            }
        } catch (JMSException e) {
            logger.error("Failed to process message", e);
        }
    }
    
}
