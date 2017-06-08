package com.mulesoft.agent.eventtracker.cloudhub.source;

/**
 * Factory for object path that used to store {@link com.mulesoft.agent.domain.tracking.FlowSourceEvent}.
 */
final class S3FlowSourceNameFactory
{

    private static final String OBJECT_NAME_TEMPLATE = "ch-%s-%s/%s/%s";

    private final String appId;

    S3FlowSourceNameFactory(String appId)
    {
        if (appId == null)
        {
            throw new NullPointerException("Cannot build S3FlowSourceNameFactory with null app id!");
        }
        this.appId = appId;
    }

    String build(String appName, String messageId, String flowName)
    {
        if (appName == null || messageId == null || flowName == null)
        {
            throw new NullPointerException("Cannot build name!");
        }
        return String.format(OBJECT_NAME_TEMPLATE, appName, appId, messageId, flowName);
    }
}
