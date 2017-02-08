package com.mulesoft.agent.monitoring.publisher;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.cloudhub.CloudhubPlatformClient;
import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.services.OnOffSwitch;

/**
 * Handler that publishes Mule messages count to CloudHub.
 */
@Named("mule.agent.cloudhub.messages.internal.handler")
@Singleton
public class CloudhubMessagePublisher extends BufferedHandler<GroupedApplicationsMetrics> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(CloudhubMessagePublisher.class);

    @Inject
    public CloudhubMessagePublisher() {
        super();
    }

    public CloudhubMessagePublisher(OnOffSwitch enabledSwitch) {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    protected boolean canHandle(GroupedApplicationsMetrics message) {
        return true;
    }

    @Override
    protected boolean flush(Collection<GroupedApplicationsMetrics> messages) {
        for (GroupedApplicationsMetrics gam : messages) {
            Map<String, ApplicationMetrics> metrics = gam.getMetricsByApplicationName();

            for (Map.Entry<String, ApplicationMetrics> entry : metrics.entrySet()) {
                List<Metric> appMetrics = entry.getValue().getMetrics();
                Metric msgCountMetric = appMetrics.stream()
                                                  .filter(m -> "messageCount".equals(m.getName()))
                                                  .findAny().get();
                long messageCount = msgCountMetric.getValue().longValue();
                long timestamp = msgCountMetric.getTimestamp();
                logger.info("got messageCount: " + messageCount + " - " + timestamp);

                MuleMessageSnapshot mms = new MuleMessageSnapshot(messageCount, timestamp);
                send(mms);
            }
        }
        return true;
    }

    private void send(MuleMessageSnapshot ms) {
        String json = "";
        try {
            json = mapper.writeValueAsString(ms);
        } catch (JsonProcessingException e) {
            logger.error("Error converting to json", e);
        }
        CloudhubPlatformClient.getClient().sendMessagesStats(json);
    }


    protected static class MuleMessageSnapshot {
        final long messageCount;
        final long timestamp;

        MuleMessageSnapshot(long messageCount, long timestamp) {
            this.messageCount = messageCount;
            this.timestamp = timestamp;
        }
    }
}
