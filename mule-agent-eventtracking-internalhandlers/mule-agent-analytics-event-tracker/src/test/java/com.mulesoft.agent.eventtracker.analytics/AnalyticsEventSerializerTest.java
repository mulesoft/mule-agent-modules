package com.mulesoft.agent.eventtracker.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AnalyticsEventSerializerTest
{
    private ObjectMapper objectMapper;

    @Before
    public void before()
    {
        objectMapper = new ObjectMapper();

        SimpleModule serializationModule = new SimpleModule("SerializationModule", new Version(1, 0, 0, null, null, null));

        serializationModule.addSerializer(new AnalyticsEventSerializerExtended());
        objectMapper.registerModule(serializationModule);
    }


    @Test
    public void whenNotificationWithUnknownTypeIsReceivedItIsNotSerialized() throws JsonProcessingException
    {
        AgentTrackingNotification agentTrackingNotification = new AgentTrackingNotification.TrackingNotificationBuilder()
                .application("applicationName")
                .timestamp(1506358801L)
                .muleMessageId("e6b90478-0652-4aea-bc52-75080c06cba9")
                .notificationType("thisIsANonExistingNotificationTypeHaha")
                .resourceIdentifier("flowName")
                .build();

        String serializedBody = objectMapper.writeValueAsString(objectMapper.writeValueAsString(agentTrackingNotification));
        Assert.assertEquals(serializedBody, "\"\"");
    }

    @Test
    public void whenNotificationWithExistingTypeIsReceivedItIsSerialized() throws JsonProcessingException
    {
        AgentTrackingNotification agentTrackingNotification = new AgentTrackingNotification.TrackingNotificationBuilder()
                .application("applicationName")
                .timestamp(1506362590786L)
                .rootMuleMessageId("cac58b10-a21b-11e7-aa5c-a45e60db6b95")
                .notificationType("EndpointMessageNotification")
                .action("receive")
                .resourceIdentifier("pollerFlow")
                .build();

        String body = "\"" +  "{" +
                    "\\\"id\\\":\\\"mockedId\\\"," +
                    "\\\"messageId\\\":\\\"cac58b10-a21b-11e7-aa5c-a45e60db6b95\\\"," +
                    "\\\"name\\\":\\\"\\\"," +
                    "\\\"type\\\":\\\"MESSAGE_RECEIVE\\\"," +
                    "\\\"timestamp\\\":1506362590786," +
                    "\\\"flowName\\\":\\\"pollerFlow\\\"," +
                    "\\\"path\\\":null," +
                    "\\\"customProperties\\\":{" +
                    "}," +
                    "\\\"systemProperties\\\":{" +
                    "}" +
                "}" + "\"";

        String serializedBody = objectMapper.writeValueAsString(objectMapper.writeValueAsString(agentTrackingNotification));
        Assert.assertEquals(serializedBody, body);
    }

    class AnalyticsEventSerializerExtended extends AnalyticsEventSerializer
    {
        @Override
        protected String getId()
        {
            return "mockedId";
        }
    }

}
