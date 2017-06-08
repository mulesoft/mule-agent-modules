package com.mulesoft.agent.eventtracker.cloudhub;

import java.util.Map;

/**
 * Insight Event for Cloudhub.
 */
public class InsightEvent
{

    /**
     * The id the event.
     */
    private String id;

    /**
     * The message id of the event.
     */
    private String messageId;

    /**
     * The name of the event.
     */
    private String name;

    /**
     * The flow name of the event.
     */
    private String flowName;

    /**
     * The time at which the event was produced.
     */
    private Long timestamp;

    /**
     * The type of the event.
     */
    private String type;

    /**
     * The system properties of the event.
     */
    private Map<String, String> systemProperties;

    /**
     * The custom properties of the event.
     */
    private Map<String, String> customProperties;

    public String getId()
    {
        return id;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public String getName()
    {
        return name;
    }

    public String getFlowName()
    {
        return flowName;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    public String getType()
    {
        return type;
    }

    public Map<String, String> getSystemProperties()
    {
        return systemProperties;
    }

    public Map<String, String> getCustomProperties()
    {
        return customProperties;
    }

    /**
     * Builder for {@link InsightEvent}.
     */
    public static class Builder
    {
        private final InsightEvent insightEvent;

        public Builder()
        {
            insightEvent = new InsightEvent();
        }

        public Builder withId(String id)
        {
            insightEvent.id = id;
            return this;
        }

        public Builder withMessageId(String messageId)
        {
            insightEvent.messageId = messageId;
            return this;
        }

        public Builder withName(String name)
        {
            insightEvent.name = name;
            return this;
        }

        public Builder withFlowName(String flowName)
        {
            insightEvent.flowName = flowName;
            return this;
        }

        public Builder withTimestamp(Long timestamp)
        {
            insightEvent.timestamp = timestamp;
            return this;
        }

        public Builder withType(String type)
        {
            insightEvent.type = type;
            return this;
        }

        public Builder withSystemProperties(Map<String, String> systemProperties)
        {
            insightEvent.systemProperties = systemProperties;
            return this;
        }

        public Builder withCustomProperties(Map<String, String> customProperties)
        {
            insightEvent.customProperties = customProperties;
            return this;
        }

        public InsightEvent build()
        {
            return insightEvent;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("InsightEvent [id=")
                .append(id)
                .append(", messageId=")
                .append(messageId)
                .append(", name=")
                .append(name)
                .append(", flowName=")
                .append(flowName)
                .append(", timestamp=")
                .append(timestamp)
                .append(", type=")
                .append(type)
                .append(", systemProperties=")
                .append(systemProperties)
                .append(", customProperties=")
                .append(customProperties)
                .append("]");
        return builder.toString();
    }

}
