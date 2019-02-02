package demo.stackoverflow.logging;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;

/**
 * @author sfcoy
 */
public class LoggerFactory {

    @Produces
    Logger produceLogger(InjectionPoint injectionPoint) {
        return org.slf4j.LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }

}
