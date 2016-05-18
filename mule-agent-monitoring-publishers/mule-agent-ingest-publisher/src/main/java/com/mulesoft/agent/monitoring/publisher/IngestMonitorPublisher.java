/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.buffer.BufferConfiguration;
import com.mulesoft.agent.buffer.BufferType;
import com.mulesoft.agent.buffer.BufferedHandler;
import com.mulesoft.agent.configuration.Configurable;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.AnypointMonitoringIngestAPIClient;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestApplicationMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestMetricBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestTargetMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.model.MetricSample;
import com.mulesoft.agent.services.OnOffSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Ingest API instance.
 * </p>
 */
@Singleton
@Named("mule.agent.ingest.metrics.internal.handler")
public class IngestMonitorPublisher extends BufferedHandler<List<Metric>>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(IngestMonitorPublisher.class);
    private static final String CPU_METRIC_NAME = "java.lang:type=OperatingSystem:CPU";
    private static final String MEMORY_USAGE_METRIC_NAME = "java.lang:type=Memory:heap used";
    private static final String MEMORY_TOTAL_METRIC_NAME = "java.lang:type=Memory:heap total";
    private static final List<String> keys = Lists.newArrayList(CPU_METRIC_NAME, MEMORY_TOTAL_METRIC_NAME, MEMORY_USAGE_METRIC_NAME);

    @Configurable("http://0.0.0.0:8070")
    private String ingestEndpoint;
    @Configurable("1")
    private String apiVersion;
    @Configurable("6c001e57-aa67-431e-b5cf-8ad1145c5f30")
    private String organizationId;
    @Configurable("7a964cfd-5cda-47b6-bcfe-817fa0f00362")
    private String environmentId;
    @Configurable("1")
    private String targetId;
    // TODO Once we actually start getting the application metrics, this process shall be removed from here for this will be a whole new publisher.
    @Configurable("1")
    private String applicationId;
    @Configurable("true")
    private Boolean enabled;

    // TODO Once we actually start getting the application metrics, this process shall be removed from here for this will be a whole new publisher.
    @Inject
    private IngestApplicationMetricPostBodyBuilder applicationMetricBuilder;
    @Inject
    private IngestTargetMetricPostBodyBuilder targetMetricBuilder;
    @Inject
    private IngestMetricBuilder metricBuilder;

    private AnypointMonitoringIngestAPIClient client;

    @Override
    protected boolean canHandle(List<Metric> metrics)
    {
        return true;
    }

    @PostConfigure
    public void postConfigurable() throws AgentEnableOperationException
    {
        if(this.enabledSwitch == null)
        {
            this.enabledSwitch = OnOffSwitch.newNullSwitch(this.enabled);
            if (this.buffer == null)
            {
                this.buffer = new BufferConfiguration();
                this.buffer.setType(BufferType.MEMORY);
                this.buffer.setRetryCount(1);
                this.buffer.setFlushFrequency(60000L);
                this.buffer.setMaximumCapacity(100);
            }
        }
        this.client = AnypointMonitoringIngestAPIClient.create(ingestEndpoint, apiVersion, organizationId, environmentId);
    }

    private IngestTargetMetricPostBody processTargetMetrics(Collection<List<Metric>> collection)
    {
        Date now = new Date();

        MetricClassification classification = new MetricClassification(keys, collection);

        IngestMetric cpuUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(CPU_METRIC_NAME)));
        IngestMetric memoryUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(MEMORY_USAGE_METRIC_NAME)));
        IngestMetric memoryTotalSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(MEMORY_TOTAL_METRIC_NAME)));

        return targetMetricBuilder.build(cpuUsageSample, memoryUsageSample, memoryTotalSample);
    }

    // TODO Once we actually start getting the application metrics, this process shall be removed from here for this will be a whole new publisher.
    private IngestApplicationMetricPostBody processApplicationMetrics(Collection<List<Metric>> collection)
    {
        Date now = new Date();

        MetricClassification classification = new MetricClassification(keys, collection);

        IngestMetric cpuUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(CPU_METRIC_NAME)));
        IngestMetric memoryUsageSample = metricBuilder.build(new MetricSample(now, classification.getMetrics(MEMORY_USAGE_METRIC_NAME)));

        double divider = Math.random() * 1000;
        double min = memoryUsageSample.getMin() == null ? 0 : memoryUsageSample.getMin() * 1000;
        double max = memoryUsageSample.getMax() == null ? 0 : memoryUsageSample.getMax() * 1000;
        double avg = memoryUsageSample.getAvg() == null ? 0 : memoryUsageSample.getAvg() * 1000;
        IngestMetric messagesCount = new IngestMetric(memoryUsageSample.getTime(), min,
                max, memoryUsageSample.getSum() * 1000, avg, memoryUsageSample.getCount());
        IngestMetric errorCount = new IngestMetric(messagesCount.getTime(), messagesCount.getMin() / divider,
                messagesCount.getMax()  / divider, messagesCount.getSum()  / divider, messagesCount.getAvg()  / divider, messagesCount.getCount());

        return applicationMetricBuilder.build(messagesCount, cpuUsageSample, errorCount);
    }

    private boolean send(IngestTargetMetricPostBody targetBody, IngestApplicationMetricPostBody appBody)
    {
        try
        {
            this.client.postTargetMetrics(targetId, targetBody);
            // TODO Once we actually start getting the application metrics, this process shall be removed from here for this will be a whole new publisher.
            this.client.postApplicationMetrics(applicationId, appBody);
            LOGGER.info("Published metrics to Ingest successfully");
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Could not publish metrics to Ingest: ", e);
            return false;
        }
    }

    @Override
    protected boolean flush(Collection<List<Metric>> collection)
    {
        IngestTargetMetricPostBody targetBody = processTargetMetrics(collection);
        // TODO Once we actually start getting the application metrics, this process shall be removed from here for this will be a whole new publisher.
        IngestApplicationMetricPostBody appBody = processApplicationMetrics(collection);
        LOGGER.info("publishing metrics to ingest api.");
        return send(targetBody, appBody);
    }
}
