package demo.stackoverflow.jms;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * @author sfcoy
 */
@Stateless
public class Delegate {

    @Inject
    private Logger logger;

    @Resource
    private SessionContext sessionContext;
    
    public void handleMessage(String message) {
        logger.info("Message \"{}\" received from {}", message, sessionContext.getCallerPrincipal());
    }
}
