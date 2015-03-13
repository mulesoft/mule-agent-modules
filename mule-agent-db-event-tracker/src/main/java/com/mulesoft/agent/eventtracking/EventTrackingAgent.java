package com.mulesoft.agent.eventtracking;


import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.tracking.AgentTrackingNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.util.Collection;

@Named("com.mulesoft.agent.eventtracking.eventtrackingagent")
@Singleton
public class EventTrackingAgent extends BufferedHandler<AgentTrackingNotification> {

    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackingAgent.class);
    @Inject
    public EventTrackingAgent()
    {
        super();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    protected boolean canHandle(@NotNull AgentTrackingNotification agentTrackingNotification) {
        return true;
    }

    @Override
    protected boolean flush(@NotNull Collection<AgentTrackingNotification> collection) {
        LOGGER.error("FUNCIONANDO!!!");
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
