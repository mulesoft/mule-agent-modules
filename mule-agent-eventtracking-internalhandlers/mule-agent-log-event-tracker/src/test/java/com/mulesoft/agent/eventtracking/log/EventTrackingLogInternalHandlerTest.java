/**
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.eventtracking.log;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.mulesoft.agent.exception.AgentEnableOperationException;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EventTrackingLogInternalHandlerTest
{
    @Test
    public void test ()
            throws AgentEnableOperationException
    {
        EventTrackingLogInternalHandler handler = new EventTrackingLogInternalHandler();
        handler.fileName = System.getProperty("fileName");
        handler.filePattern = System.getProperty("filePattern");
        handler.setBufferSize(Integer.parseInt(System.getProperty("bufferSize")));
        handler.setImmediateFlush(Boolean.parseBoolean(System.getProperty("immediateFlush")));
        handler.setDaysTrigger(Integer.parseInt(System.getProperty("daysTrigger")));
        handler.setMbTrigger(Integer.parseInt(System.getProperty("mbTrigger")));
        handler.setEnabled(Boolean.parseBoolean(System.getProperty("enabled")));
        handler.setDateFormatPattern(System.getProperty("dateFormatPattern"));
        handler.postConfigurable();

        for (AgentTrackingNotification notification : createNotifications())
        {
            handler.handle(notification);
        }
    }

    private List<AgentTrackingNotification> createNotifications ()
    {
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for (int i = 0; i < 100000; i++)
        {
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .build());
        }
        return list;
    }
}
