package com.mulesoft.agent.monitoring.publisher;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.common.internalhandler.cloudhub.CloudhubPlatformClient;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.services.OnOffSwitch;

/**
 * Handler that publishes JMX memory information to CloudHub.
 */
@Named("mule.agent.cloudhub.memory.internal.handler")
@Singleton
public class CloudhubMemoryMetricPublisher
        extends BufferedHandler<List<Metric>> {

    @Inject
    private CloudhubPlatformClient client;

    private MemorySnapshot lastSnapshot;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger(
            CloudhubMemoryMetricPublisher.class);

    @Configurable("false")
    private boolean enabled;

    @Configurable
    private BufferConfiguration buffer;

    @Inject
    public CloudhubMemoryMetricPublisher() {
        super();
    }

    public CloudhubMemoryMetricPublisher(OnOffSwitch enabledSwitch) {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    protected CloudhubMemoryMetricPublisher(CloudhubPlatformClient client) {
        super();
        this.client = client;
    }

    @Override
    protected boolean canHandle(List<Metric> metrics) {
        return true;
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection) {
        for (List<Metric> sample : collection) {
            Metric memoryMaxMetric = sample.stream()
                                           .filter(m ->
                                                   SupportedJMXBean.HEAP_TOTAL.getMetricName().equals(m.getName()))
                                           .findAny().get();
            long memoryMax = memoryMaxMetric.getValue().longValue();
            long timestamp = memoryMaxMetric.getTimestamp();

            long memoryUsed = sample.stream()
                                    .filter(m ->
                                            SupportedJMXBean.HEAP_USAGE.getMetricName().equals(m.getName()))
                                    .findAny().get()
                                    .getValue().longValue();

            lastSnapshot = new MemorySnapshot(memoryMax, memoryUsed,
                    timestamp);
            send(lastSnapshot);
        }
        return true;
    }

    private void send(MemorySnapshot ms) {
        String json;
        try {
            json = MAPPER.writeValueAsString(ms);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error converting to json", e);
            throw new RuntimeException(e);
        }
        client.sendMemoryStats(json);
    }

    protected MemorySnapshot getLastSnapshot() {
        return lastSnapshot;
    }


    protected static class MemorySnapshot {
        public final long memoryTotalMaxBytes;
        public final long memoryTotalUsedBytes;
        public final double memoryPercentUsed;
        public final long timestamp;

        MemorySnapshot(long memoryTotalMaxBytes, long memoryTotalUsedBytes,
                       long timestamp) {
            this.memoryTotalMaxBytes = memoryTotalMaxBytes;
            this.memoryTotalUsedBytes = memoryTotalUsedBytes;
            this.memoryPercentUsed =
                    ((double) memoryTotalUsedBytes / memoryTotalMaxBytes) * 100D;
            this.timestamp = timestamp;
        }
    }
}
