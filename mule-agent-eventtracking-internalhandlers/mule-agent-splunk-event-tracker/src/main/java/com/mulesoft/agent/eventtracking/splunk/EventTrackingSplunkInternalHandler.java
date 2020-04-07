/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracking.splunk;

import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.common.internalhandler.serializer.mixin.AgentTrackingNotificationMixin;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import com.mulesoft.agent.handlers.exception.InitializationException;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <p>
 * The Splunk Internal handler will store all the Event Notifications produced from the Mule ESB flows in Splunk instance.
 * </p>
 */
@Singleton
@Named("mule.agent.tracking.handler.splunk")
public class EventTrackingSplunkInternalHandler extends AbstractSplunkInternalHandler<AgentTrackingNotification>
{
    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();

        this.getObjectMapper().addMixInAnnotations(AgentTrackingNotification.class, AgentTrackingNotificationMixin.class);
    }
}
