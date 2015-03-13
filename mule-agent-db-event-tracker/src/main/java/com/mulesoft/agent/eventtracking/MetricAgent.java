package com.mulesoft.agent.eventtracking;


import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.monitoring.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

@Named("com.mulesoft.agent.eventtracking.metricagent")
@Singleton
public class MetricAgent extends BufferedHandler<List<Metric>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MetricAgent.class);
    @Inject
    public MetricAgent()
    {
        super();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    protected boolean canHandle(List<Metric> metricList) {
        return true;
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection) {
        LOGGER.error("FUNCIONANDO LIST METRIC!!!");
        try {
            PrintWriter writer = new PrintWriter("C:\\agent-metric.txt", "UTF-8");
            for(List<Metric> notification : collection){
                for(Metric n : notification) {
                    writer.println(n.getName() + "|" + n.getValue());
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
