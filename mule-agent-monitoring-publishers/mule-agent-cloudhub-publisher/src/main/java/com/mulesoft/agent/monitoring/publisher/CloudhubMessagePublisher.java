package com.mulesoft.agent.monitoring.publisher;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.monitoring.ApplicationMetrics;
import com.mulesoft.agent.domain.monitoring.GroupedApplicationsMetrics;
import com.mulesoft.agent.services.OnOffSwitch;

/**
 * Handler that publishes Mule messages information to CloudHub.
 */
@Named("mule.agent.cloudhub.messages.internal.handler")
@Singleton
public class CloudhubMessagePublisher extends BufferedHandler<GroupedApplicationsMetrics> {

    private CloseableHttpClient client;

    private static final Logger logger = LogManager.getLogger(CloudhubMessagePublisher.class);

    @Inject
    public CloudhubMessagePublisher() {
        super();
    }

    public CloudhubMessagePublisher(OnOffSwitch enabledSwitch) {
        super();
        this.enabledSwitch = enabledSwitch;
        this.client = HttpClients.createDefault();
    }

    @Override
    protected boolean canHandle(GroupedApplicationsMetrics message) {
        return true;
    }

    @Override
    protected boolean flush(Collection<GroupedApplicationsMetrics> messages) {
        logger.info("got stuff, size of metrics: " + messages.size());
        for (GroupedApplicationsMetrics gam : messages) {
            Map<String, ApplicationMetrics> metrics = gam.getMetricsByApplicationName();
            logger.info("more sizes: " + metrics.size());

            for (Map.Entry entry : metrics.entrySet()) {
                logger.info("key: " + entry.getKey() + " - value: " + entry.getValue());
            }
        }

        return true;
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
