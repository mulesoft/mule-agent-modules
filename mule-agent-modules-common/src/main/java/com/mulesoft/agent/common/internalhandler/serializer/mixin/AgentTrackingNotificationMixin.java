package com.mulesoft.agent.common.internalhandler.serializer.mixin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mulesoft.agent.common.internalhandler.serializer.TimestampToDateSerializer;

/**
 * Agent tracking notification mixin.
 */
public abstract class AgentTrackingNotificationMixin
{
    @JsonSerialize(using = TimestampToDateSerializer.class)
    abstract long getTimestamp();
}
