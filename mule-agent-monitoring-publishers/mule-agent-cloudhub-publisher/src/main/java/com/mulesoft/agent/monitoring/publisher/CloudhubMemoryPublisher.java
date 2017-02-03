package com.mulesoft.agent.monitoring.publisher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.util.EC2MetadataUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.services.OnOffSwitch;

/**
 * Handler that publishes JMX memory information to CloudHub.
 */
@Named("mule.agent.cloudhub.memory.internal.handler")
@Singleton
public class CloudhubMemoryPublisher extends BufferedHandler<List<Metric>> {

    private CloseableHttpClient client;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(CloudhubMemoryPublisher.class);

    @Inject
    public CloudhubMemoryPublisher() {
        super();
        this.client = HttpClients.createDefault();
    }

    public CloudhubMemoryPublisher(OnOffSwitch enabledSwitch) {
        super();
        this.enabledSwitch = enabledSwitch;
    }

    @Override
    protected boolean canHandle(List<Metric> metrics) {
        return true;
    }

    /**
     * Processes JMX metrics and sends them to CloudHub's platform-services over HTTP.
     *
     * @param collection the JMX data sample
     * @return whether the flush was successful
     */
    @Override
    protected boolean flush(Collection<List<Metric>> collection) {
        for (List<Metric> sample : collection) {
            Metric memoryMaxMetric = sample.stream()
                                           .filter(m -> SupportedJMXBean.HEAP_TOTAL.getMetricName().equals(m.getName()))
                                           .findAny().get();
            long memoryMax = memoryMaxMetric.getValue().longValue();
            long timestamp = memoryMaxMetric.getTimestamp();
            logger.info("memoryMax: " + memoryMax);
            logger.info("timestamp: " + timestamp);

            long memoryUsed = sample.stream()
                                    .filter(m -> SupportedJMXBean.HEAP_USAGE.getMetricName().equals(m.getName()))
                                    .findAny().get()
                                    .getValue().longValue();
            logger.info("memoryUsed: " + memoryUsed);

            MemorySnapshot ms = new MemorySnapshot(memoryMax, memoryUsed, timestamp);
            logger.info("memorySnapshot created - " + ms);
            sendToPlatform(ms);
        }
        return true;
    }

    private void sendToPlatform(MemorySnapshot ms) {
        HttpPost req = new HttpPost("https://platformhost.com");

        String json = "";
        try {
            json = mapper.writeValueAsString(ms);
        } catch (JsonProcessingException e) {
            logger.error("Error converting to json", e);
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(json);
        } catch (UnsupportedEncodingException e) { // ignore
        }

        req.setHeader("Content-type", "application/json");
        req.setEntity(entity);

        try (CloseableHttpResponse res = client.execute(req)) {
            // do stuff

            EntityUtils.consumeQuietly(res.getEntity());
        } catch (IOException e) {
            logger.error("Exception trying to call platform", e);
        }
    }


    private static class MemorySnapshot {
        private final long timestamp;
        private final String instanceId;
        private final long memoryTotalMaxBytes;
        private final long memoryTotalUsedBytes;
        private final double memoryPercentUsed;

        MemorySnapshot(long memoryTotalMaxBytes, long memoryTotalUsedBytes, long timestamp) {
            this.timestamp = timestamp;
            this.instanceId = EC2MetadataUtils.getInstanceId();
            this.memoryTotalMaxBytes = memoryTotalMaxBytes;
            this.memoryTotalUsedBytes = memoryTotalUsedBytes;
            this.memoryPercentUsed = ((double) memoryTotalUsedBytes / memoryTotalMaxBytes) * 100D;
        }
    }
}
