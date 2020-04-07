/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport.config;

import com.mulesoft.agent.exception.AgentConfigurationException;
import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.common.internalhandler.splunk.DummySplunkInternalHandler;
import com.splunk.SSLSecurityProtocol;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Walter Poch
 *         created on 10/28/15
 */
public class RestTransportConfigTest
{
    @Test
    public void canCreateACorrectConfig() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        RestTransportConfig config = new RestTransportConfig.Builder(internalHandler).build();

        assertEquals(internalHandler.getUser(), config.getUser());
        assertEquals(internalHandler.getPass(), config.getPass());
        assertEquals(internalHandler.getHost(), config.getHost());
        assertEquals(internalHandler.getPort(), config.getPort());
        assertEquals(internalHandler.getScheme(), config.getScheme().getValue());
        assertEquals(SSLSecurityProtocol.valueOf(internalHandler.getSslSecurityProtocol()), config.getSslSecurityProtocol());
        assertEquals(internalHandler.getSplunkIndexName(), config.getIndex());
        assertEquals(internalHandler.getSplunkSource(), config.getSource());
        assertEquals(internalHandler.getSplunkSourceType(), config.getSourceType());
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfAConfigurationErrorIsFound() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setScheme("htt");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoHostIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoPassIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoUserIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoPortIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSchemeIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSslSecurityProtocolIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoIndexIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSourceIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSourceType("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSourceTypeIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setUser("testUser");
        internalHandler.setPass("testPass");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");

        new RestTransportConfig.Builder(internalHandler).build();
    }
}
