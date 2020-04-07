/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracking.tryit;

import static com.mulesoft.agent.services.MessageHistory.MESSAGE_HISTORY_INTERNAL_HANDLER;
import static java.util.stream.Collectors.groupingBy;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.services.MessageHistory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The message history internal handler will handle the notification to a {@link MessageHistory} once the buffer is flushed.
 * </p>
 */
@Singleton
@Named(MESSAGE_HISTORY_INTERNAL_HANDLER)
public class EventTrackingMessageHistoryInternalHandler extends BufferedHandler<AgentTrackingNotification>
{

    @Inject
    private MessageHistory messageHistory;

    @Override
    protected boolean canHandle(AgentTrackingNotification message)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> messages)
    {
        Map<String, List<AgentTrackingNotification>> groupByApplication = messages.stream().collect(groupingBy(AgentTrackingNotification::getApplication));
        groupByApplication.entrySet().stream().forEach(entry -> messageHistory
            .bufferAgentTrackingNotifications(entry.getKey(), entry.getValue()));
        return true;
    }

}
