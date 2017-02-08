package com.mulesoft.agent.monitoring.publisher;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.cloudhub.CloudhubPlatformClient;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.services.OnOffSwitch;

/**
 * Handler that publishes JMX memory information to CloudHub.
 */
@Named("mule.agent.cloudhub.memory.internal.handler")
@Singleton
public class CloudhubMemoryPublisher extends BufferedHandler<List<Metric>> {

    protected MemorySnapshot lastSnapshot;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(CloudhubMemoryPublisher.class);

    @Inject
    public CloudhubMemoryPublisher() {
        super();
    }

    public CloudhubMemoryPublisher(OnOffSwitch enabledSwitch) {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    protected boolean canHandle(List<Metric> metrics) {
        return true;
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection) {
        for (List<Metric> sample : collection) {
            Metric memoryMaxMetric = sample.stream()
                                           .filter(m -> SupportedJMXBean.HEAP_TOTAL.getMetricName().equals(m.getName()))
                                           .findAny().get();
            long memoryMax = memoryMaxMetric.getValue().longValue();
            long timestamp = memoryMaxMetric.getTimestamp();
            logger.debug("memoryMax: " + memoryMax);
            logger.debug("timestamp: " + timestamp);

            long memoryUsed = sample.stream()
                                    .filter(m -> SupportedJMXBean.HEAP_USAGE.getMetricName().equals(m.getName()))
                                    .findAny().get()
                                    .getValue().longValue();
            logger.debug("memoryUsed: " + memoryUsed);

            lastSnapshot = new MemorySnapshot(memoryMax, memoryUsed, timestamp);
            send(lastSnapshot);
        }
        return true;
    }

    private void send(MemorySnapshot ms) {
        String json = "";
        try {
            json = mapper.writeValueAsString(ms);
        } catch (JsonProcessingException e) {
            logger.error("Error converting to json", e);
        }
        CloudhubPlatformClient.getClient().sendMemoryStats(json);
    }


    protected static class MemorySnapshot {
        final long memoryTotalMaxBytes;
        final long memoryTotalUsedBytes;
        final double memoryPercentUsed;
        final long timestamp;

        MemorySnapshot(long memoryTotalMaxBytes, long memoryTotalUsedBytes, long timestamp) {
            this.memoryTotalMaxBytes = memoryTotalMaxBytes;
            this.memoryTotalUsedBytes = memoryTotalUsedBytes;
            this.memoryPercentUsed = ((double) memoryTotalUsedBytes / memoryTotalMaxBytes) * 100D;
            this.timestamp = timestamp;
        }
    }
}
