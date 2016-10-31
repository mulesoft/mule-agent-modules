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
import com.splunk.SSLSecurityProtocol;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Walter Poch
 *         created on 10/28/15
 */
public class HECTransportConfigTest
{
    @Test
    public void canCreateACorrectConfig() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";
        internalHandler.acceptAnyCertificate = true;

        HECTransportConfig config = new HECTransportConfig.Builder(internalHandler).build();

        assertEquals(internalHandler.token, config.getToken());
        assertEquals(internalHandler.host, config.getHost());
        assertEquals(internalHandler.port, config.getPort());
        assertEquals(internalHandler.scheme, config.getScheme().getValue());
        assertEquals(SSLSecurityProtocol.valueOf(internalHandler.sslSecurityProtocol), config.getSslSecurityProtocol());
        assertEquals(internalHandler.splunkIndexName, config.getIndex());
        assertEquals(internalHandler.splunkSource, config.getSource());
        assertEquals(internalHandler.splunkSourceType, config.getSourceType());
        assertEquals(internalHandler.acceptAnyCertificate, config.getAcceptAnyCertificate());
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfAConfigurationErrorIsFound() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.scheme = "htt";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoHostIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        //internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoTokenIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        //internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoPortIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        //internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSchemeIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        //internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSslSecurityProtocolIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        //internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoIndexIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        //internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSourceIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        //internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }

    @Test(expected = AgentConfigurationException.class)
    public void exceptionThrowIfNoSourceTypeIsProvided() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.token = "testToken";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        //internalHandler.splunkSourceType = "mule";

        new HECTransportConfig.Builder(internalHandler).build();
    }
}
