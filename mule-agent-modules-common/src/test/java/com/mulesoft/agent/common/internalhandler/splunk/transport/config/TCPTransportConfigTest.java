/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport.config;

import com.mulesoft.agent.AgentConfigurationException;
import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.common.internalhandler.splunk.DummySplunkInternalHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Walter Poch
 *         created on 10/28/15
 */
public class TCPTransportConfigTest
{
    @Test
    public void canCreateACorrectConfig() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);

        TCPTransportConfig config = new TCPTransportConfig.Builder(internalHandler).build();

        assertEquals(internalHandler.getHost(), config.getHost());
        assertEquals(internalHandler.getPort(), config.getPort());
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoHostIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setPort(8089);

        new TCPTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoPortIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setHost("127.0.0.1");

        new TCPTransportConfig.Builder(internalHandler).build();
    }
}
