package com.mulesoft.agent.eventtracker.cloudhub.tracking;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.eventtracker.cloudhub.InsightEvent;

public class AgentNotificationTransformerTest
{

    @Test
    public void testToInsightEvent()
    {
        AgentTrackingNotification notification = createNotification("type");
        InsightEvent event = AgentNotificationTransformer.toInsightEvent(notification);
        assertEquals(notification.getRootMuleMessageId(), event.getMessageId());
        assertEquals(notification.getResourceIdentifier(), event.getFlowName());
        assertEquals(new Long(notification.getTimestamp()), event.getTimestamp());
        assertEquals(notification.getCustomEventProperties(), event.getCustomProperties());

        assertEquals("", event.getType());
        assertEquals(notification.getResourceIdentifier(), event.getName());
        assertEquals(notification.getCorrelationId(), event.getSystemProperties().get("MESSAGE_CORRELATION_ID"));
        assertEquals(notification.getTransactionId(), event.getSystemProperties().get("CUSTOM_TRANSACTION_ID"));
        assertEquals(notification.getApplication(), event.getSystemProperties().get("APPLICATION_NAME"));
    }

    @Test
    public void testInsightEventName()
    {
        assertEquals("flowName",
                AgentNotificationTransformer.toInsightEvent(createNotification("AsyncMessageNotification")).getName());
        assertEquals("flowName", AgentNotificationTransformer
                .toInsightEvent(createNotification("ExceptionStrategyNotification")).getName());
        assertEquals("flowName", AgentNotificationTransformer
                .toInsightEvent(createNotification("PipelineMessageNotification")).getName());
        assertEquals("flowName",
                AgentNotificationTransformer.toInsightEvent(createNotification("ExceptionNotification")).getName());
        assertEquals("flowName", AgentNotificationTransformer
                .toInsightEvent(createNotification("EndpointMessageNotification")).getName());

        assertEquals("source", AgentNotificationTransformer
                .toInsightEvent(createNotification("ComponentMessageNotification")).getName());
        assertEquals("source",
                AgentNotificationTransformer.toInsightEvent(createNotification("EventNotification")).getName());
        assertEquals("source", AgentNotificationTransformer
                .toInsightEvent(createNotification("MessageProcessorNotification")).getName());
        assertEquals("source",
                AgentNotificationTransformer.toInsightEvent(createNotification("TransactionNotification")).getName());
        assertEquals("source",
                AgentNotificationTransformer.toInsightEvent(createNotification("BatchNotification")).getName());
        assertEquals("source", AgentNotificationTransformer
                .toInsightEvent(createNotification("ConnectorMessageNotification")).getName());
    }

    @Test
    public void testInsightEventSystemProperties()
    {
        assertEquals("source", AgentNotificationTransformer.toInsightEvent(createNotification("ExceptionNotification"))
                .getSystemProperties()
                .get("EXCEPTION_DETAILS"));
        assertEquals("source", AgentNotificationTransformer.toInsightEvent(createNotification("ComponentNotification"))
                .getSystemProperties()
                .get("EXCEPTION_DETAILS"));
        assertEquals("source", AgentNotificationTransformer.toInsightEvent(createNotification("ComponentNotification"))
                .getSystemProperties()
                .get("COMPONENT_CLASS"));
    }

    private AgentTrackingNotification createNotification(String notificationType)
    {
        return new AgentTrackingNotification.TrackingNotificationBuilder().rootMuleMessageId("rootMuleMessageId")
                .resourceIdentifier("flowName")
                .timestamp(0)
                .source("source")
                .notificationType(notificationType)
                .application("application")
                .transactionId("transactionId")
                .customEventProperties(Collections.singletonMap("key", "value"))
                .build();
    }
}
