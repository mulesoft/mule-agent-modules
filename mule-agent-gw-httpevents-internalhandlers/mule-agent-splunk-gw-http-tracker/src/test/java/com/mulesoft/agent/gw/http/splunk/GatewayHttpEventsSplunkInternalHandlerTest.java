package com.mulesoft.agent.gw.http.splunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.module.client.model.HttpEvent;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

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
        for (HttpEvent notification : createNotifications())
        {
            success &= handler.handle(notification);
        }

        Assert.assertTrue(success);
    }

    private List<HttpEvent> createNotifications ()
    {
        List<HttpEvent> list = new ArrayList<HttpEvent>();
        for (int i = 0; i < 1000; i++)
        {
            list.add(new HttpEvent(i, i, "ORG_ID", "HOST_ID", "CLIENT", "TRANSACTION", "1",
                    "192.168.1.1", "GET", "/path", 200, "AGENT", 100, 100, "", "", ""));
        }
        return list;
    }
}
