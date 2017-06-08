package com.mulesoft.agent.eventtracker.cloudhub.tracking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.eventtracker.analytics.AnalyticsEventType;
import com.mulesoft.agent.eventtracker.cloudhub.InsightEvent;

/**
 * Transformer that transforms {@link AgentTrackingNotification} to {@link InsightEvent}.
 */
final class AgentNotificationTransformer
{
    private AgentNotificationTransformer()
    {
    }

    static InsightEvent toInsightEvent(AgentTrackingNotification notification)
    {
        InsightEvent.Builder eventBuilder = new InsightEvent.Builder();
        eventBuilder.withId(UUID.randomUUID().toString());
        eventBuilder.withMessageId(notification.getRootMuleMessageId());
        eventBuilder.withFlowName(notification.getResourceIdentifier());
        eventBuilder.withTimestamp(notification.getTimestamp());

        eventBuilder.withName(getName(notification));
        AnalyticsEventType type = AnalyticsEventType.getAnalyticsEventType(notification);
        eventBuilder.withType(type == null ? "" : type.name());

        eventBuilder.withCustomProperties(notification.getCustomEventProperties());
        eventBuilder.withSystemProperties(getSystemProperties(notification));

        return eventBuilder.build();
    }

    private static String getName(AgentTrackingNotification notification)
    {
        if (notification.getNotificationType() == null)
        {
            return null;
        }
        switch (notification.getNotificationType())
        {
        case "AsyncMessageNotification":
        case "ExceptionStrategyNotification":
        case "PipelineMessageNotification":
        case "ExceptionNotification":
            // flow name
            // TODO ideally Exception Notification has the name of the Exception Type, which is not currently available
            return notification.getResourceIdentifier();
        case "ComponentMessageNotification":
        case "EventNotification":
        case "MessageProcessorNotification":
        case "TransactionNotification":
            // simple class name
            // transaction id in case of transaction notification
            return notification.getSource();
        // listener NOT implemented
        case "BatchNotification":
            // "job name" started on "create time"
            return notification.getSource();
        case "ConnectorMessageNotification":
            // probably, the name should be
            return notification.getSource();
        // deprecated
        case "EndpointMessageNotification":
        default:
            return notification.getResourceIdentifier();
        }
    }

    private static Map<String, String> getSystemProperties(AgentTrackingNotification notification)
    {
        if (notification.getNotificationType() == null)
        {
            return null;
        }
        Map<String, String> properties = new HashMap<>();
        switch (notification.getNotificationType())
        {
        case "ComponentNotification":
            properties.put("COMPONENT_CLASS", notification.getSource());
            // passing through on purpose
        case "ExceptionNotification":
            if (StringUtils.isNotBlank(notification.getSource()))
            {
                properties.put("EXCEPTION_DETAILS", notification.getSource());
            }
            // need exception type
            break;
        default:
            break;
        }
        if (notification.getCorrelationId() != null)
        {
            properties.put("MESSAGE_CORRELATION_ID", notification.getCorrelationId());
        }
        if (notification.getTransactionId() != null)
        {
            properties.put("CUSTOM_TRANSACTION_ID", notification.getTransactionId());
        }
        properties.put("APPLICATION_NAME", notification.getApplication());
        return properties;
    }

}
