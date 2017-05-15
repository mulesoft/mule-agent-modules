package com.mulesoft.agent.gw.http.log;

import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.module.client.model.HttpEvent;

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

        for (HttpEvent notification : createNotifications())
        {
            handler.handle(notification);
        }
    }

    private List<HttpEvent> createNotifications ()
    {
        List<HttpEvent> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++)
        {
            list.add(new HttpEvent(i, i, "ORG_ID", "HOST_ID", "CLIENT", "TRANSACTION", "1",
                    "192.168.1.1", "GET", "/path", 200, "AGENT", 100, 100, "", "", ""));
        }
        return list;
    }
}
