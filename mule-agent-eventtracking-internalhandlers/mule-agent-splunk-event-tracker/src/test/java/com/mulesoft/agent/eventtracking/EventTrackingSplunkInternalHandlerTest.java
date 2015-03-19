package com.mulesoft.agent.eventtracking;

import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventTrackingSplunkInternalHandlerTest {

    @Test
    public void test() throws IOException {
        EventTrackingSplunkInternalHandler handler = new EventTrackingSplunkInternalHandler();
        handler.user = "admin";
        handler.pass = "test";
        handler.host = "192.168.61.128";
        handler.port = 8089;
        handler.scheme = "https";
        handler.splunkIndexName = "main";
        handler.splunkSource = "test";
        handler.splunkSourceType = "mule-test";
        handler.postConfigurable();

        boolean success = handler.flush(createNotifications());
        Assert.assertTrue(success);
    }

    private List<AgentTrackingNotification> createNotifications(){
        List<AgentTrackingNotification> list = new ArrayList<AgentTrackingNotification>();
        for(int i = 0; i < 20000; i++){
            list.add(new AgentTrackingNotification.TrackingNotificationBuilder()
                    .action("TEST " + i)
                    .annotations(new ArrayList<Annotation>())
                    .timestamp(new Date().getTime())
                    .application("Splunk TEST")
                    .build());
        }
        return list;
    }
}
