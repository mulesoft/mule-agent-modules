/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.common.internalhandler.splunk.transport;

import com.mulesoft.agent.exception.AgentConfigurationException;
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
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8088);
        internalHandler.setScheme("tcp");

        Transport transport = new DefaultTransportFactory(internalHandler).create();

        Assert.isInstanceOf(TCPTransport.class, transport);
    }

    @Test
    public void canCreateRestTransport() throws AgentConfigurationException
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

        Transport transport = new DefaultTransportFactory(internalHandler).create();

        Assert.isInstanceOf(RestTransport.class, transport);
    }

    @Test
    public void canCreateHECTransport() throws AgentConfigurationException
    {
        AbstractSplunkInternalHandler internalHandler = new DummySplunkInternalHandler();
        internalHandler.setToken("testToken");
        internalHandler.setHost("127.0.0.1");
        internalHandler.setPort(8089);
        internalHandler.setScheme("https");
        internalHandler.setSslSecurityProtocol("TLSv1_2");
        internalHandler.setSplunkIndexName("main");
        internalHandler.setSplunkSource("mule");
        internalHandler.setSplunkSourceType("mule");
        internalHandler.setAcceptAnyCertificate(true);

        Transport transport = new DefaultTransportFactory(internalHandler).create();

        Assert.isInstanceOf(HECTransport.class, transport);
    }
}
