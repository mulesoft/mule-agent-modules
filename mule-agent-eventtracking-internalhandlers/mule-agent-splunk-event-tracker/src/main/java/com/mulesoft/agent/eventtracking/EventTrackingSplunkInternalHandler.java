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
import java.io.IOException;
import java.io.OutputStream;
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

        // DEBUG
        IndexCollectionArgs indexcollArgs = new IndexCollectionArgs();
        indexcollArgs.setSortKey("totalEventCount");
        indexcollArgs.setSortDirection(IndexCollectionArgs.SortDirection.DESC);
        IndexCollection myIndexes = service.getIndexes(indexcollArgs);

        // List the indexes and their event counts
        System.out.println("There are " + myIndexes.size() + " indexes:\n");
        for (Index entity: myIndexes.values()) {
            System.out.println("  " + entity.getName() + " (events: "
                    + entity.getTotalEventCount() + ")");
        }
    }

    @Override
    protected boolean canHandle(AgentTrackingNotification message) {
        return true;
    }

    @Override
    protected boolean flush(final Collection<AgentTrackingNotification> messages) {
        LOGGER.trace(String.format("Flushing %s notifications.", messages.size()));
        try {
            index.attachWith(new ReceiverBehavior() {
                @Override
                public void run(OutputStream stream) throws IOException {
                    for (AgentTrackingNotification notification : messages){
                        LOGGER.trace("Flushing Notification: " + notification);
                        stream.write((notification.toString()+"\r\n").getBytes("UTF8"));
                    }
                }
            });
            return true;
        } catch (IOException e) {
            LOGGER.error("There was an error sending the notifications to the Splunk instance.", e);
            return false;
        }
    }
}
