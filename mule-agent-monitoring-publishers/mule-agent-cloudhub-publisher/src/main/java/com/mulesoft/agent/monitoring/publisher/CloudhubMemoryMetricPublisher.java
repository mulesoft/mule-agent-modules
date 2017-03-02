package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.client.CloudhubPlatformClient;
import com.mulesoft.agent.monitoring.publisher.factory.MemorySnapshotFactory;
import com.mulesoft.agent.services.OnOffSwitch;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Handler that publishes JMX memory information to CloudHub.
 */
@Named("mule.agent.cloudhub.memory.internal.handler")
@Singleton
public class CloudhubMemoryMetricPublisher
        extends BufferedHandler<List<Metric>>
{

    private static final Logger LOGGER = LogManager.getLogger(
            CloudhubMemoryMetricPublisher.class);

    @Configurable("true")
    private boolean enabled;

    @Inject
    private CloudhubPlatformClient cloudhubClient;
    @Inject
    private MemorySnapshotFactory factory;

    public CloudhubMemoryMetricPublisher()
    {
    }

    protected CloudhubMemoryMetricPublisher(
            CloudhubPlatformClient client, MemorySnapshotFactory factory)
    {
        super();
        this.cloudhubClient = client;
        this.factory = factory;
    }

    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if (this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
        }
    }

    @Override
    protected boolean canHandle(List<Metric> metrics)
    {
        return true;
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection)
    {
        boolean success = false;
        try
        {
            for (List<Metric> sample : collection)
            {
                Optional<Metric> memoryTotalMetric = sample.stream()
                        .filter(m ->
                                SupportedJMXBean.HEAP_TOTAL.getMetricName().equals(m.getName()))
                        .findAny();
                if (!memoryTotalMetric.isPresent())
                {
                    LOGGER.info(String.format("No %s metric found",
                            SupportedJMXBean.HEAP_TOTAL.getMetricName()));
                    continue;
                }
                Metric metric = memoryTotalMetric.get();
                long memoryTotal = metric.getValue().longValue();
                long timestamp = metric.getTimestamp();

                Optional<Metric> memoryUsedMetric = sample.stream()
                        .filter(m ->
                                SupportedJMXBean.HEAP_USAGE.getMetricName().equals(m.getName()))
                        .findAny();
                if (!memoryUsedMetric.isPresent())
                {
                    LOGGER.info(String.format("No %s metric found",
                            SupportedJMXBean.HEAP_USAGE.getMetricName()));
                    continue;
                }
                long memoryUsed = memoryUsedMetric.get()
                        .getValue().longValue();

                success = cloudhubClient.sendMemoryStats(
                        factory.newSnapshot(memoryTotal, memoryUsed, timestamp));
            }
            return success;
        }
        catch (Exception e)
        {
            LOGGER.error("Error flushing memory metrics to CloudHub: {}",
                    ExceptionUtils.getRootCauseMessage(e));
            LOGGER.debug(e);
            return success;
        }
    }
}
