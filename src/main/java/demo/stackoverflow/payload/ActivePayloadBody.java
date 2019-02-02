package demo.stackoverflow.payload;

import java.security.AccessController;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import demo.stackoverflow.jms.Delegate;

/**
 * @author sfcoy
 */
public class ActivePayloadBody implements ActivePayload {

    private static final Logger logger = LoggerFactory.getLogger(ActivePayloadBody.class);

    // This was a bit much to hope for...
    @Context
    private SecurityContext securityContext;

    @Override
    public void run() {
        // A breakpoint here will show that all "security identity" related fields in the stack are null
        if (securityContext != null) {
            logger.info("Running contextual JMS payload from user \"{}\"", securityContext.getUserPrincipal());
        } else {
            // As described in §3.3.1.1.5 Task Definition of Concurrency Utilities for Java EE 1.0
            Subject currentSubject = Subject.getSubject(AccessController.getContext());
            logger.info("Running contextual JMS payload with subject \"{}\"", currentSubject);
        }
    }
    
}
