/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
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

/**
 * <p>
 * Serializer that converts an {@link AgentTrackingNotification} to the JSON format supported by
 * the Analytics service
 * </p>
 */
public class AnalyticsEventSerializer extends JsonSerializer<AgentTrackingNotification> {

    @Override
    public Class<AgentTrackingNotification> handledType() {
        return AgentTrackingNotification.class;
    }

    @Override
    public void serialize(AgentTrackingNotification value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        try {
            AnalyticsEventType analyticsEventType = AnalyticsEventType.getAnalyticsEventType(value);
            jgen.writeStartObject();
            jgen.writeStringField("id", UUID.randomUUID().toString());
            jgen.writeStringField("messageId", value.getRootMuleMessageId());
            jgen.writeStringField("name", value.getNotificationType());
            jgen.writeStringField("type", analyticsEventType == null ? "" : analyticsEventType.name());
            jgen.writeNumberField("timestamp", value.getTimestamp());
            jgen.writeStringField("flowName", value.getResourceIdentifier());
            jgen.writeStringField("path", value.getPath());
            jgen.writeFieldName("customProperties");
            jgen.writeStartObject();
            if (value.getCustomEventProperties() != null) {
                for (Map.Entry<String, String> property : value.getCustomEventProperties().entrySet()) {
                    jgen.writeStringField(property.getKey(), property.getValue());
                }
            }
            jgen.writeEndObject();
            jgen.writeFieldName("systemProperties");
            jgen.writeStartObject();
            if (value.getNotificationType().equals("ExceptionNotification")) {
                jgen.writeStringField("EXCEPTION_DETAILS", value.getSource());
            }
            if (value.getCorrelationId() != null) {
                jgen.writeStringField("MESSAGE_CORRELATION_ID", value.getCorrelationId());
            }
            if (value.getTransactionId() != null) {
                jgen.writeStringField("CUSTOM_TRANSACTION_ID", value.getTransactionId());
            }
            jgen.writeEndObject();
            jgen.writeEndObject();
        } catch (IOException e) {
        }
    }
}
