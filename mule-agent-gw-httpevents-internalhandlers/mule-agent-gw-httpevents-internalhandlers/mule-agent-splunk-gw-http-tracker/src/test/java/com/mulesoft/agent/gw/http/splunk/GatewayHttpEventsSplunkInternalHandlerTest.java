/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.splunk;

import static com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent;
import com.mulesoft.mule.runtime.gw.api.analytics.RequestDisposition;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GatewayHttpEventsSplunkInternalHandlerTest
{

    @Test
    public void test ()
            throws IOException, AgentEnableOperationException, InitializationException
    {
        GatewayHttpEventsSplunkInternalHandler handler = new GatewayHttpEventsSplunkInternalHandler();
        handler.setEnabled(true);
        handler.setUser(System.getProperty("user"));
        handler.setPass(System.getProperty("pass"));
        handler.setHost(System.getProperty("host"));
        handler.setPort(Integer.parseInt(System.getProperty("port")));
        handler.setScheme(System.getProperty("scheme"));
        handler.setSslSecurityProtocol(System.getProperty("sslSecurityProtocol"));
        handler.setSplunkIndexName(System.getProperty("splunkIndexName"));
        handler.setSplunkSource(System.getProperty("splunkSource"));
        handler.setSplunkSourceType(System.getProperty("splunkSourceType"));
        handler.setDateFormatPattern(System.getProperty("dateFormatPattern"));

        handler.postConfigurable();
        handler.initialize();

        boolean success = true;
        for (AnalyticsHttpEvent notification : createNotifications())
        {
            success &= handler.handle(notification);
        }

        Assert.assertTrue(success);
    }

    private List<AnalyticsHttpEvent> createNotifications ()
    {
        List<AnalyticsHttpEvent> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
        {
            AnalyticsHttpEvent event =
                builder()
                    .withApiId(4605)
                    .withApiName("zTest Proxy")
                    .withApiVersion("Rest")
                    .withApiVersionId(46672L)
                    .withClientIp("127.0.0.1")
                    .withEventId("8a0e3d60-7cfc-11e5-82f4-0a0027000000")
                    .withOrgId("66310c16-bce5-43c4-b978-5945ed2f99c5")
                    .withPath("/gateway/proxy/apikit/items ")
                    .withReceivedTs("2015-10-27T19:46:19.447-03:00")
                    .withRepliedTs("2015-10-27T19:46:19.532-03:00")
                    .withRequestBytes(-1)
                    .withResponseBytes(132)
                    .withStatusCode(200)
                    .withUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36 ")
                    .withVerb("GET")
                    .withRequestDisposition(RequestDisposition.PROCESSED)
                    .build();

            list.add(event);
        }
        return list;
    }
}
