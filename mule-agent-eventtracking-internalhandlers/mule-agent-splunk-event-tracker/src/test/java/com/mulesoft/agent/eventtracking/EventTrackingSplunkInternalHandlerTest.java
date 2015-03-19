package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class EventTrackingSplunkInternalHandlerTest {

    @Test
    public void test(){
        EventTrackingSplunkInternalHandler handler = new EventTrackingSplunkInternalHandler();
        handler.user = "admin";
        handler.pass = "test";
        handler.host = "192.168.61.128";
        handler.port = 8089;
        handler.scheme = "https";
        handler.postConfigurable();

        handler.flush(createNotifications());

        Assert.fail();
    }

    private List<AgentTrackingNotification> createNotifications(){
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for(int i = 0; i < 10; i++){
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .build());
        }
        return list;
    }
}
