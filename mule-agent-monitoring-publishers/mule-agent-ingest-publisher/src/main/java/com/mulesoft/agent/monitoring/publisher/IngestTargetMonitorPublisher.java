/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestTargetMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Ingest API instance.
 * </p>
 */
@Singleton
@Named("mule.agent.ingest.target.metrics.internal.handler")
public class IngestTargetMonitorPublisher extends IngestMonitorPublisher<List<Metric>>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(IngestTargetMonitorPublisher.class);

    private static final String CPU_METRIC_NAME = "java.lang:type=OperatingSystem:CPU";
    private static final String MEMORY_USAGE_METRIC_NAME = "java.lang:type=Memory:heap used";
    private static final String MEMORY_TOTAL_METRIC_NAME = "java.lang:type=Memory:heap total";
    private static List<String> keys = Arrays.asList(CPU_METRIC_NAME, MEMORY_TOTAL_METRIC_NAME, MEMORY_USAGE_METRIC_NAME);

    @Inject
    private IngestTargetMetricPostBodyBuilder targetMetricBuilder;

    private IngestTargetMetricPostBody processTargetMetrics(Collection<List<Metric>> collection)
    {
        Date now = new Date();

        MetricClassification classification = new MetricClassification(keys, collection);

        List<Metric> cpuMetrics = classification.getMetrics(CPU_METRIC_NAME);
        List<Metric> memoryUsageMetrics = classification.getMetrics(MEMORY_USAGE_METRIC_NAME);
        List<Metric> memoryTotalMetrics = classification.getMetrics(MEMORY_TOTAL_METRIC_NAME);
        LOGGER.info("found " + (cpuMetrics != null ? cpuMetrics.size() : 0) + " cpu metrics, " +
                (memoryUsageMetrics != null ? memoryUsageMetrics.size() : 0) + " memory usage metrics and " +
                (memoryTotalMetrics != null ? memoryTotalMetrics.size() : 0) + " memory total metrics");

        IngestMetric cpuUsageSample = metricBuilder.build(new CPUMetricSampleDecorator(new DefaultMetricSample(now, cpuMetrics)));
        IngestMetric memoryUsageSample = metricBuilder.build(new MemoryMetricSampleDecorator(new DefaultMetricSample(now, memoryUsageMetrics)));
        IngestMetric memoryTotalSample = metricBuilder.build(new MemoryMetricSampleDecorator(new DefaultMetricSample(now, memoryTotalMetrics)));

        return targetMetricBuilder.build(cpuUsageSample, memoryUsageSample, memoryTotalSample);
    }

    protected boolean send(Collection<List<Metric>> collection)
    {
        try
        {
            IngestTargetMetricPostBody targetBody = this.processTargetMetrics(collection);
            boolean result = this.client.postTargetMetrics(targetBody);
            if (result)
            {
                LOGGER.info("Published target metrics to Ingest successfully");
            }
            else
            {
                LOGGER.error("Could not publish target metrics to Ingest");
            }
            return result;
        }
        catch (Exception e)
        {
            LOGGER.error("Could not publish target metrics to Ingest: ", e);
            return false;
        }
    }

}
