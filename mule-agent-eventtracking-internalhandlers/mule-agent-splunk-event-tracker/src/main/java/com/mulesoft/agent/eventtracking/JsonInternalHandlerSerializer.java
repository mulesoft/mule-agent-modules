package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonInternalHandlerSerializer implements InternalHandlerSerializer<String>
{
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonInternalHandlerSerializer.class);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public String serialize (AgentTrackingNotification notification)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Notification to serialize: " + notification);
        }

        String serialized = "{ " +
                "\"timestamp\": \"" + dateFormat.format(new Date(notification.getTimestamp())) + "\", " +
                "\"application\": \"" + notification.getApplication() + "\", " +
                "\"notificationType\": \"" + notification.getNotificationType() + "\", " +
                "\"action\": \"" + notification.getAction() + "\", " +
                "\"resourceIdentifier\": \"" + notification.getResourceIdentifier() + "\", " +
                "\"source\": \"" + notification.getSource() + "\", " +
                "\"muleMessage\": \"" + notification.getMuleMessage() + "\", " +
                "\"path\": \"" + notification.getPath() + "\", " +
                "\"annotations\": \"" + notification.getAnnotations().size() + "\", " +
                "\"muleMessageId\": \"" + notification.getMuleMessageId() + "\"" +
                " }\r\n";

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Serialized notification: " + serialized);
        }
        return serialized;
    }
}
