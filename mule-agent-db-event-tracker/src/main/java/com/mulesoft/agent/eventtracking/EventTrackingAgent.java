package com.mulesoft.agent.eventtracking;


import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.util.Collection;

@Named("as.agent.eventtracking.eventtrackingagent")
@Singleton
public class EventTrackingAgent extends BufferedHandler<AgentTrackingNotification> {

    @Override
    protected boolean canHandle(AgentTrackingNotification agentTrackingNotification) {
        return true;
    }

    @Override
    protected boolean flush(Collection<AgentTrackingNotification> collection) {
        try {
            PrintWriter writer = new PrintWriter("C:\\agent.txt", "UTF-8");
            for(AgentTrackingNotification notification : collection){
                writer.println(notification.getAction()+"|"+notification.getPath());
            }
            writer.close();
            return true;
        }catch (Exception ex){
            return false;
        }
    }
}
