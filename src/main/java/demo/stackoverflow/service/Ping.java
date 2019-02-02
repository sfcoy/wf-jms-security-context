package demo.stackoverflow.service;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ContextService;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;

import demo.stackoverflow.payload.ActivePayload;
import demo.stackoverflow.payload.ActivePayloadBody;

/**
 * @author sfcoy
 */
@Path("")
@Stateless
public class Ping {

    @Inject
    private Logger logger;
    
    @Context
    private SecurityContext securityContext;

    @Resource
    ContextService contextService;
    
    @Resource(lookup = "java:/queues/demoQueue")
    private Queue demoQueue;

    @Resource
    private ConnectionFactory connectionFactory;

    /**
     * I thought this was worth a shot. Doesn't work though...
     * Inspired by <a href="http://activemq.apache.org/how-should-i-use-the-vm-transport.html">How should I use the VM transport</a>
     */
    @PostConstruct
    void initConnectionFactory() {
        try {
            Method setObjectMessageSerializationDefered = connectionFactory.getClass()
                    .getMethod("setObjectMessageSerializationDefered", Boolean.TYPE);
            setObjectMessageSerializationDefered.invoke(connectionFactory, true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.warn("No setObjectMessageSerializationDefered found");
        }
    }

    @GET
    @Path("ping")
    @RolesAllowed("Admin")
    public String ping() throws JMSException {
        try (final Connection connection = connectionFactory.createConnection();
             final Session session = connection.createSession();
             final MessageProducer messageProducer = session.createProducer(demoQueue)) {
            final ActivePayload activePayload = new ActivePayloadBody();

            Map<String, String> executionProperties = new LinkedHashMap<>();
            final ActivePayload activePayloadProxy
                    = contextService.createContextualProxy(activePayload, executionProperties, ActivePayload.class);
            final ObjectMessage objectMessage = session.createObjectMessage(activePayloadProxy);
            objectMessage.setStringProperty("source", "ping");
            messageProducer.send(objectMessage);
        }
        return securityContext != null ? "OK " + securityContext.getUserPrincipal().getName() : "OK";
    }

    /**
     * This was just checking the unlikely event that MDBs had no access to WildFly EJB 3 internals.
     * It works just fine though.
     */
    @GET
    @Path("ping2")
    @RolesAllowed("Admin")
    public String ping2() throws JMSException {
        final long timestamp = System.currentTimeMillis();
        try (final Connection connection = connectionFactory.createConnection();
             final Session session = connection.createSession();
             final MessageProducer messageProducer = session.createProducer(demoQueue)) {
            final TextMessage textMessage = session.createTextMessage("Hello from ping2 sent at " + timestamp);
            textMessage.setStringProperty("source", "ping2");
            messageProducer.send(textMessage);
            logger.info("Text message queued");
        }
        return "Message queued " + timestamp;
    }

}
