/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracker.analytics;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Serializer that converts an {@link AgentTrackingNotification} to the JSON format supported by
 * the Analytics service.
 */
public class AnalyticsEventSerializer extends JsonSerializer<AgentTrackingNotification>
{

    private static final Logger LOGGER = LogManager.getLogger(AnalyticsEventSerializer.class);

    @Override
    public Class<AgentTrackingNotification> handledType()
    {
        return AgentTrackingNotification.class;
    }

    @Override
    public void serialize(AgentTrackingNotification value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
    {
        try
        {
            AnalyticsEventType analyticsEventType = AnalyticsEventType.getAnalyticsEventType(value);
            if (analyticsEventType.equals(AnalyticsEventType.UNKNOWN))
            {
                LOGGER.debug("Serializing notification of UNKNOWN type from application {}.", value.getApplication());
                return;
            }

            jgen.writeStartObject();
            jgen.writeStringField("id", getId());
            jgen.writeStringField("messageId", value.getRootMuleMessageId());
            jgen.writeStringField("name", value.getCustomEventName() == null ? "" : value.getCustomEventName());
            jgen.writeStringField("type", analyticsEventType.name());
            jgen.writeNumberField("timestamp", value.getTimestamp());
            jgen.writeStringField("flowName", value.getResourceIdentifier());
            jgen.writeStringField("path", value.getPath());
            jgen.writeFieldName("customProperties");
            jgen.writeStartObject();
            if (value.getCustomEventProperties() != null)
            {
                for (Map.Entry<String, String> property : value.getCustomEventProperties().entrySet())
                {
                    jgen.writeStringField(property.getKey(), property.getValue());
                }
            }
            jgen.writeEndObject();
            jgen.writeFieldName("systemProperties");
            jgen.writeStartObject();
            if (value.getNotificationType().equals("ExceptionNotification"))
            {
                jgen.writeStringField("EXCEPTION_DETAILS", value.getSource());
            }
            if (value.getNotificationType().equals("ComponentNotification"))
            {
                jgen.writeStringField("COMPONENT_CLASS", value.getSource());
            }
            if (value.getCorrelationId() != null)
            {
                jgen.writeStringField("MESSAGE_CORRELATION_ID", value.getCorrelationId());
            }
            if (value.getTransactionId() != null)
            {
                jgen.writeStringField("CUSTOM_TRANSACTION_ID", value.getTransactionId());
            }
            jgen.writeEndObject();
            jgen.writeEndObject();
        }
        catch (IOException e)
        {
        }
    }

    protected String getId()
    {
        return UUID.randomUUID().toString();
    }
}
