package com.mulesoft.agent.analytics.gateway.log;

import com.mulesoft.agent.domain.analytics.gateway.GatewayHttpMetric;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Ignore
public class SplunkGatewayHttpMetricsHandlerTest
{

    @Test
    public void test () throws IOException
    {
        SplunkGatewayHttpMetricsHandler handler = new SplunkGatewayHttpMetricsHandler();
        handler.user = System.getProperty("user");
        handler.pass = System.getProperty("pass");
        handler.host = System.getProperty("host");
        handler.port = Integer.parseInt(System.getProperty("port"));
        handler.scheme = System.getProperty("scheme");
        handler.splunkIndexName = System.getProperty("splunkIndexName");
        handler.splunkSource = System.getProperty("splunkSource");
        handler.splunkSourceType = System.getProperty("splunkSourceType");
        handler.postConfigurable();

        boolean success = false;

        for (List<GatewayHttpMetric> notifications : createNotifications())
        {
            success &= handler.handle(notifications);
        }

        Assert.assertTrue(success);
    }

    private List<List<GatewayHttpMetric>> createNotifications ()
    {
        List<GatewayHttpMetric> list = new ArrayList<>();
        for (int i = 0; i < 20000; i++)
        {
            list.add(new GatewayHttpMetric(i, i, "ORG_ID", "HOST_ID", "CLIENT", "TRANSACTION", "1",
                                           "192.168.1.1", "GET", "/path", 200, "AGENT", 100, 100));
        }
        return Collections.singletonList(list);
    }
}
