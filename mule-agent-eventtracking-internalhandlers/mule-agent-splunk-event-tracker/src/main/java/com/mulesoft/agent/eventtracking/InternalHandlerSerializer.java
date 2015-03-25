package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

/**
 * Created by Walter on 3/25/2015.
 */
public interface InternalHandlerSerializer<T> {
    T serialize(AgentTrackingNotification notification);
}
