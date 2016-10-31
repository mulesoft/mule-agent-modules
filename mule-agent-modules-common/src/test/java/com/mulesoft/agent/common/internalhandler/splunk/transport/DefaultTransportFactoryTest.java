/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.mulesoft.agent.AgentConfigurationException;
import com.mulesoft.agent.common.internalhandler.AbstractSplunkInternalHandler;
import com.mulesoft.agent.common.internalhandler.splunk.DummySplunkInternalHandler;
import org.junit.Test;
import org.springframework.util.Assert;
/**
 * @author Walter Poch
 *         created on 10/29/15
 */
public class DefaultTransportFactoryTest
{
    @Test
    public void canCreateTcpTransport() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8088;
        internalHandler.scheme = "tcp";

        Transport transport = new DefaultTransportFactory(internalHandler).create();

        Assert.isInstanceOf(TCPTransport.class, transport);
    }

    @Test
    public void canCreateRestTransport() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.user = "testUser";
        internalHandler.pass = "testPass";
        internalHandler.host = "127.0.0.1";
        internalHandler.port = 8089;
        internalHandler.scheme = "https";
        internalHandler.sslSecurityProtocol = "TLSv1_2";
        internalHandler.splunkIndexName = "main";
        internalHandler.splunkSource = "mule";
        internalHandler.splunkSourceType = "mule";

        Transport transport = new DefaultTransportFactory(internalHandler).create();

        Assert.isInstanceOf(RestTransport.class, transport);
    }

    @Test
    public void canCreateHECTransport() throws AgentConfigurationException
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

        Transport transport = new DefaultTransportFactory(internalHandler).create();

        Assert.isInstanceOf(HECTransport.class, transport);
    }
}
