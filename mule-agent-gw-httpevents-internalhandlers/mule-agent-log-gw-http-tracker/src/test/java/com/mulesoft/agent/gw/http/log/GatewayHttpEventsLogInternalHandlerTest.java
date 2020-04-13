/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.gw.http.log;

import static com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent.builder;

import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.mule.runtime.gw.api.analytics.AnalyticsHttpEvent;
import com.mulesoft.mule.runtime.gw.api.analytics.RequestDisposition;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GatewayHttpEventsLogInternalHandlerTest
{
    @Test
    public void test ()
            throws AgentEnableOperationException, InitializationException
    {
        GatewayHttpEventsLogInternalHandler handler = new GatewayHttpEventsLogInternalHandler();
        handler.fileName = System.getProperty("fileName");
        handler.filePattern = System.getProperty("filePattern");
        handler.setBufferSize(Integer.parseInt(System.getProperty("bufferSize")));
        handler.setImmediateFlush(Boolean.parseBoolean(System.getProperty("immediateFlush")));
        handler.setDaysTrigger(Integer.parseInt(System.getProperty("daysTrigger")));
        handler.setMbTrigger(Integer.parseInt(System.getProperty("mbTrigger")));
        handler.setEnabled(Boolean.parseBoolean(System.getProperty("enabled")));
        handler.setDateFormatPattern(System.getProperty("dateFormatPattern"));
        handler.postConfigurable();
        handler.initialize();

        for (AnalyticsHttpEvent notification : createNotifications())
        {
            handler.handle(notification);
        }
    }

    private List<AnalyticsHttpEvent> createNotifications ()
    {
        List<AnalyticsHttpEvent> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++)
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
