WildFly 15 JMS Contextual Proxy Woes
==

This application demonstrates some issues with WildFly 15 and the use of
 [javax.enterprise.concurrent.ContextService.createContextualProxy(T, java.util.Map<java.lang.String,java.lang.String>, java.lang.Class<T>)](https://javaee.github.io/javaee-spec/javadocs/javax/enterprise/concurrent/ContextService.html#createContextualProxy-T-java.util.Map-java.lang.Class-).
 It evolved while attempting to solve [Wildfly ContextService concurrent securityIdentity is null
](https://stackoverflow.com/questions/54310392/wildfly-contextservice-concurrent-securityidentity-is-null/54357390#54357390)
for a user on Stack Overflow. 

Set up the demonstration program.
--

 1. Install WildFly 15.0.1 Final
 2. Copy the two demo-* files from the `bin` directory to $WILDFLY_HOME/standalone/configuration/
 3. Configure the server
 
        $WILDFLY_HOME/bin/jboss-cli.sh --file=bin/configure.cli
        
 4. Build and deploy the demo application
 
Issue 1
--

 1. Attempt to access the URL http://localhost:8080/wf-jms-security-context/ping
 2. You will be asked for BASIC authentication credentials - use `user1/password123`
 3. The page should show "OK user1"
 
 The WildFly server log will have a stack trace:
 
    16:23:31,360 ERROR [demo.stackoverflow.jms.ContextualMessageHandler] (Thread-33 (ActiveMQ-client-global-threads)) Failed to process message: javax.jms.JMSException: org.jboss.as.ejb3.component.concurrent.EJBContextHandleFactory$EJBContextHandle from [Module "org.apache.activemq.artemis" version 2.6.3.jbossorg-00014 from local module loader @79698539 (finder: local module finder @73f792cf (roots: /Users/steve/servers/wildfly-15.0.1.Final/modules,/Users/steve/servers/wildfly-15.0.1.Final/modules/system/layers/base))]
        at org.jboss.modules.ModuleClassLoader.findClass(ModuleClassLoader.java:255)
        at org.jboss.modules.ConcurrentClassLoader.performLoadClassUnchecked(ConcurrentClassLoader.java:410)
        at org.jboss.modules.ConcurrentClassLoader.performLoadClass(ConcurrentClassLoader.java:398)
        ...
 
This appears to be a consequence of the `org.apache.activemq.artemis` module needing access to classes in 
the `org.jboss.as.ejb3` module.

In any event adding `org.jboss.as.ejb3` to `org.apache.activemq.artemis` resolves the exception. I don't know if that is
the best place for it though.

This leads to...
 
Issue 2
--
 
The `demo.stackoverflow.jms.ContextualMessageHandler` receives a deserialized `demo.stackoverflow.payload.ActivePayloadBody` 
proxy and invokes its `run` method. This call does not appear to have access to to the original callers security 
identity.

Inspecting the proxy that is created at line 64 of `demo.stackoverflow.service.Ping.ping` shows that it has a 
`org.jboss.as.ee.concurrent.IdentityAwareProxyInvocationHandler` instance variable.

The source code for this IdentityAwareProxyInvocationHandler reveals:

    private final transient SecurityIdentity securityIdentity;

which clearly does not survive serialization.

[How should I use the VM transport](http://activemq.apache.org/how-should-i-use-the-vm-transport.html) suggests that
serialization can be disabled, but I could not determine if this can be configured in WildFly right now.
