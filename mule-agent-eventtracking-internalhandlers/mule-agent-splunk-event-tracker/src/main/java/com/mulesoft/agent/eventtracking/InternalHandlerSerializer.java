package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

public interface InternalHandlerSerializer<T>
{
    T serialize (AgentTrackingNotification notification);
}
