package com.mulesoft.agent.eventtracker.cloudhub.tracking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.eventtracker.cloudhub.InsightClient;
import com.mulesoft.agent.eventtracker.cloudhub.InsightEvent;
import com.mulesoft.agent.eventtracker.cloudhub.InsightException;

/**
 * An internal handler that sends Cloudhub {@link InsightEvent} to Analytics Insight.
 */
@Singleton
@Named("mule.agent.tracking.handler.cloudhub.event")
public class EventTrackingCloudhubInternalHandler extends BufferedHandler<AgentTrackingNotification>
{

    private static final Logger LOGGER = LogManager.getLogger(EventTrackingCloudhubInternalHandler.class);

    @Inject
    private InsightClient insightClient;

    EventTrackingCloudhubInternalHandler()
    {
    }

    EventTrackingCloudhubInternalHandler(InsightClient insightClient)
    {
        this.insightClient = insightClient;
    }

    @Override
    protected boolean canHandle(AgentTrackingNotification message)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> notifications)
    {
        LOGGER.debug("Flushing events to cloudhub platform");
        try
        {
            List<InsightEvent> events = new ArrayList<>(notifications.size());
            for (AgentTrackingNotification notification : notifications)
            {
                events.add(AgentNotificationTransformer.toInsightEvent(notification));
            }
            insightClient.sendInsight(events);
            return true;
        }
        catch (InsightException ex)
        {
            LOGGER.warn("Could not send tracking event to the Analytics service for application. Error: {}",
                    ExceptionUtils.getRootCauseMessage(ex));
            LOGGER.debug(ex);
        }
        return false;
    }

}
