/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.handlers.InternalMessageHandler;
import com.mulesoft.agent.services.OnOffSwitch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Abstract monitor publisher.
 * @param <M> Message type.
 */
public abstract class AbstractMonitorPublisher<M> implements InternalMessageHandler<M>
{
    private static final Logger LOGGER = LogManager.getLogger(AbstractMonitorPublisher.class);

    @Configurable("true")
    boolean enabled;

    @PostConfigure
    public void createSwitcher()
    {
        enabledSwitch = OnOffSwitch.newNullSwitch(enabled);
    }

    @Override
    public void enable(boolean state) throws AgentEnableOperationException
    {
        enabledSwitch.switchTo(state);
    }

    @Override
    public boolean isEnabled()
    {
        return enabledSwitch.isEnabled();
    }

    OnOffSwitch enabledSwitch;

    @Override
    public boolean handle(M metrics)
    {
        if (this.isEnabled())
        {
            return doHandle(metrics);
        }

        LOGGER.debug("Skipped handling of message, handler is not enabled");

        return false;
    }

    protected abstract boolean doHandle(M metrics);

}
