package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Named("mule.agent.tracking.handler.splunk")
@Singleton
public class EventTrackingSplunkInternalHandler extends BufferedHandler<AgentTrackingNotification> {
    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingSplunkInternalHandler.class);

    @Override
    protected boolean canHandle(AgentTrackingNotification message) {
        return false;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> messages) {
        return false;
    }
}
