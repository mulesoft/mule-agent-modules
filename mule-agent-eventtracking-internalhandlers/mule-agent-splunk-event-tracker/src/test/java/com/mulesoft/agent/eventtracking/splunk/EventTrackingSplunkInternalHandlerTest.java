package com.mulesoft.agent.eventtracking.splunk;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Ignore
public class EventTrackingSplunkInternalHandlerTest
{

    @Test
    public void test ()
            throws IOException, AgentEnableOperationException
    {
        EventTrackingSplunkInternalHandler handler = new EventTrackingSplunkInternalHandler();
        handler.setEnabled(true);
        handler.user = System.getProperty("user");
        handler.pass = System.getProperty("pass");
        handler.host = System.getProperty("host");
        handler.port = Integer.parseInt(System.getProperty("port"));
        handler.scheme = System.getProperty("scheme");
        handler.sslSecurityProtocol = System.getProperty("sslSecurityProtocol");
        handler.splunkIndexName = System.getProperty("splunkIndexName");
        handler.splunkSource = System.getProperty("splunkSource");
        handler.splunkSourceType = System.getProperty("splunkSourceType");
        handler.dateFormatPattern = System.getProperty("dateFormatPattern");

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
