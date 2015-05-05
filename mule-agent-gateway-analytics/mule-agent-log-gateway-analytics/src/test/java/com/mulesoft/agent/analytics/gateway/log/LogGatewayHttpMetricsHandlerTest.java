package com.mulesoft.agent.analytics.gateway.log;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.domain.analytics.gateway.GatewayHttpMetric;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Ignore
public class LogGatewayHttpMetricsHandlerTest
{
    @Test
    public void test () throws AgentEnableOperationException
    {
        LogGatewayHttpMetricsHandler handler = new LogGatewayHttpMetricsHandler();
        handler.pattern = System.getProperty("pattern");
        handler.fileName = System.getProperty("fileName");
        handler.filePattern = System.getProperty("filePattern");
        handler.bufferSize = Integer.parseInt(System.getProperty("bufferSize"));
        handler.inmediateFlush = Boolean.parseBoolean(System.getProperty("inmediateFlush"));
        handler.daysTrigger = Integer.parseInt(System.getProperty("daysTrigger"));
        handler.mbTrigger = Integer.parseInt(System.getProperty("mbTrigger"));
        handler.enabled = Boolean.parseBoolean(System.getProperty("enabled"));
        handler.dateFormatPattern = System.getProperty("dateFormatPattern");
        handler.postConfigurable();

        for (List<GatewayHttpMetric> notification : createNotifications())
        {
            handler.handle(notification);
        }
    }

    private List<List<GatewayHttpMetric>> createNotifications()
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
