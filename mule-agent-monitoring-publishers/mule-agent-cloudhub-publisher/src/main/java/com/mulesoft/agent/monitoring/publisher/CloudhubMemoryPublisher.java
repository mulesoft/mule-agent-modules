package com.mulesoft.agent.monitoring.publisher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

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
    private String instanceId;
    protected MemorySnapshot lastSnapshot;

    private static final String PLATFORM_SERVICES_HOST_KEY = "platform.services.endpoint";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(CloudhubMemoryPublisher.class);

    @Inject
    public CloudhubMemoryPublisher() {
        super();
    }

    public CloudhubMemoryPublisher(OnOffSwitch enabledSwitch) {
        super();
        this.enabledSwitch = enabledSwitch;
        this.client = HttpClients.createDefault();
        // check aws-java-sdk dependency
        this.instanceId = EC2MetadataUtils.getInstanceId();
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
            logger.debug("memoryMax: " + memoryMax);
            logger.debug("timestamp: " + timestamp);

            long memoryUsed = sample.stream()
                                    .filter(m -> SupportedJMXBean.HEAP_USAGE.getMetricName().equals(m.getName()))
                                    .findAny().get()
                                    .getValue().longValue();
            logger.debug("memoryUsed: " + memoryUsed);

            lastSnapshot = new MemorySnapshot(memoryMax, memoryUsed, timestamp);
//            send(lastSnapshot);
        }
        return true;
    }

    private void send(MemorySnapshot ms) {
        // broken
        String host = PLATFORM_SERVICES_HOST_KEY;
        String endpoint = String.format("%s/agentstats/memory/%s",
                host,
                instanceId);
        HttpPost req = new HttpPost(endpoint);

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
            if (res.getStatusLine().getStatusCode() != 200) {
                logger.error("Error sending metrics to platform, got status code {}",
                        res.getStatusLine().getStatusCode());
            }

            EntityUtils.consumeQuietly(res.getEntity());
        } catch (IOException e) {
            logger.error("Exception trying to call platform", e);
        }
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
