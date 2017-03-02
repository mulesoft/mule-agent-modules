package com.mulesoft.agent.eventtracking.splunk;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;

@Ignore
public class EventTrackingSplunkInternalHandlerTest
{

    @Test
    public void test ()
            throws IOException, AgentEnableOperationException
    {
        EventTrackingSplunkInternalHandler handler = new EventTrackingSplunkInternalHandler();
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

        boolean success = true;
        for (AgentTrackingNotification notification : createNotifications())
        {
            success &= handler.handle(notification);
        }

        Assert.assertTrue(success);
    }

    private List<AgentTrackingNotification> createNotifications ()
    {
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for (int i = 0; i < 1000; i++)
        {
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST Reflection" + i)
                    .annotations(new ArrayList<Annotation>())
                    .timestamp(new Date().getTime())
                    .application("SplunkTEST-Reflection")
                    .build());
        }
        return list;
    }
}
