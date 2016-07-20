/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestTargetMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.model.CPUMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.model.MemoryMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        Set<IngestMetric> cpuUsage = Sets.newHashSet();
        Set<IngestMetric> memoryUsage = Sets.newHashSet();
        Set<IngestMetric> memoryTotal = Sets.newHashSet();

        for (List<Metric> sample : collection)
        {
            MetricClassification classification = new MetricClassification(keys, sample);

            List<Metric> cpuMetrics = classification.getMetrics(CPU_METRIC_NAME);
            List<Metric> memoryUsageMetrics = classification.getMetrics(MEMORY_USAGE_METRIC_NAME);
            List<Metric> memoryTotalMetrics = classification.getMetrics(MEMORY_TOTAL_METRIC_NAME);
            LOGGER.debug("found " + (cpuMetrics != null ? cpuMetrics.size() : 0) + " cpu metrics, " +
                    (memoryUsageMetrics != null ? memoryUsageMetrics.size() : 0) + " memory usage metrics and " +
                    (memoryTotalMetrics != null ? memoryTotalMetrics.size() : 0) + " memory total metrics");

            if (cpuMetrics != null)
            {
                cpuUsage.add(
                        metricBuilder.build(new CPUMetricSampleDecorator(new DefaultMetricSample(cpuMetrics)))
                );
            }

            if (memoryUsageMetrics != null)
            {
                memoryUsage.add(
                        metricBuilder.build(new MemoryMetricSampleDecorator(new DefaultMetricSample(memoryUsageMetrics)))
                );
            }

            if (memoryTotalMetrics != null)
            {
                memoryTotal.add(
                        metricBuilder.build(new MemoryMetricSampleDecorator(new DefaultMetricSample(memoryTotalMetrics)))
                );
            }
        }

        return targetMetricBuilder.build(cpuUsage, memoryUsage, memoryTotal);
    }

    protected boolean send(Collection<List<Metric>> collection)
    {
        LOGGER.info("publishing target metrics to ingest api.");
        try
        {
            IngestTargetMetricPostBody targetBody = this.processTargetMetrics(collection);
            int responseCode = this.client.postTargetMetrics(targetBody);
            boolean result = isSuccessStatusCode(responseCode) || (isClientErrorStatusCode(responseCode) && !SUPPORTED_RETRY_CLIENT_ERRORS.contains(responseCode));
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
