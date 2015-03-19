package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.splunk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Named("mule.agent.tracking.handler.splunk")
@Singleton
public class EventTrackingSplunkInternalHandler extends BufferedHandler<AgentTrackingNotification> {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingSplunkInternalHandler.class);

    @Configurable
    String user;

    @Configurable
    String pass;

    @Configurable
    String host;

    @Configurable("8089")
    int port;

    @Configurable("https")
    String scheme;

    @Configurable("mule")
    String indexName;

    private Service service;
    private Index index;


    @PostConfigure
    public void postConfigurable(){
        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername(this.user);
        loginArgs.setPassword(this.pass);
        loginArgs.setHost(this.host);
        loginArgs.setPort(this.port);
        loginArgs.setScheme(this.scheme);

        service = Service.connect(loginArgs);
        index = service.getIndexes().get(this.indexName);
        if(index == null){
            index = service.getIndexes().create(this.indexName);
        }
    }

    @Override
    protected boolean canHandle(AgentTrackingNotification message) {
        return false;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> messages) {
        for (AgentTrackingNotification notification : messages){
            Args args = new Args();
            args.put("sourcetype", "mule-events");
            args.put("host", "laptop");
            index.submit(args, notification.toString());
        }
        return false;
    }
}
