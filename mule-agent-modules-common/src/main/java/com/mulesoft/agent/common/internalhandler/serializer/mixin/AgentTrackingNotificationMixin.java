/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

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
