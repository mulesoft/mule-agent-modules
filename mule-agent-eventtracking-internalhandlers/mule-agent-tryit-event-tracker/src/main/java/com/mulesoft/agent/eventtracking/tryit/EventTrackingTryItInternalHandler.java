/*
 * (c) 2003-2016 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracking.tryit;

import static com.mulesoft.agent.services.TryItService.TRY_IT_INTERNAL_HANDLER;
import static java.util.stream.Collectors.groupingBy;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.services.TryItService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Try It Internal handler will handle the notification to a {@link TryItService} once the buffer is flushed.
 * </p>
 */
@Singleton
@Named(TRY_IT_INTERNAL_HANDLER)
public class EventTrackingTryItInternalHandler extends BufferedHandler<AgentTrackingNotification>
{

    @Inject
    private TryItService tryItService;

    @Override
    protected boolean canHandle(AgentTrackingNotification message)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> messages)
    {
        Map<String, List<AgentTrackingNotification>> groupByApplication = messages.stream().collect(groupingBy(AgentTrackingNotification::getApplication));
        groupByApplication.entrySet().stream().forEach(entry -> tryItService.bufferAgentTrackingNotifications(entry.getKey(), entry.getValue()));
        return true;
    }

}
