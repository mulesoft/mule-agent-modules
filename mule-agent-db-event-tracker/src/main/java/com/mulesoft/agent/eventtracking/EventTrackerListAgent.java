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
import java.util.List;

@Named("com.mulesoft.agent.eventtracking.eventtrackerlistagent")
@Singleton
public class EventTrackerListAgent extends BufferedHandler<List<AgentTrackingNotification>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(EventTrackerListAgent.class);
    @Inject
    public EventTrackerListAgent()
    {
        super();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    protected boolean canHandle(@NotNull List<AgentTrackingNotification> agentTrackingNotification) {
        return true;
    }

    @Override
    protected boolean flush(@NotNull Collection<List<AgentTrackingNotification>> collection) {
        LOGGER.error("FUNCIONANDO LIST EVENT!!!");
        try {
            PrintWriter writer = new PrintWriter("C:\\agent-l.txt", "UTF-8");
            for(List<AgentTrackingNotification> notification : collection){
                for(AgentTrackingNotification n : notification) {
                    writer.println(n.getAction() + "|" + n.getPath());
                }
                writer.println("------------");
            }
            writer.close();
            return true;
        }catch (Exception ex){
            return false;
        }
    }
}
