package com.mulesoft.agent.eventtracking.http;

import com.mulesoft.agent.common.internalhandler.HttpCustomInternalHandler;
import com.mulesoft.agent.common.internalhandler.serializer.mixin.AgentTrackingNotificationMixin;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.handlers.exception.InitializationException;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("mule.agent.tracking.handler.httpCustom")
public class EventTrackingHttpInternalHandler extends HttpCustomInternalHandler<AgentTrackingNotification>
{
    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();

        this.getObjectMapper().addMixInAnnotations(AgentTrackingNotification.class, AgentTrackingNotificationMixin.class);
    }
}
