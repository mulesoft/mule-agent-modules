/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Sets;
import com.mulesoft.agent.configuration.NotAvailableOn;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestTargetMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.model.CPUMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.model.MemoryMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.ning.http.client.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.mulesoft.agent.domain.RuntimeEnvironment.ON_PREM;
import static com.mulesoft.agent.domain.RuntimeEnvironment.STANDALONE;

/**
 * <p>
 * Handler that publishes JMX information obtained from the Monitoring Service to a running Ingest API instance.
 * </p>
 */
@Singleton
@Named("mule.agent.ingest.target.metrics.internal.handler")
@NotAvailableOn(environment = {ON_PREM, STANDALONE})
public class IngestTargetMonitorPublisher extends IngestMonitorPublisher<List<Metric>>
{

    private final static Logger LOGGER = LogManager.getLogger(IngestTargetMonitorPublisher.class);

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

            if (cpuMetrics != null && cpuMetrics.size() > 0)
            {
                cpuUsage.add(
                        metricBuilder.build(new CPUMetricSampleDecorator(new DefaultMetricSample(cpuMetrics)))
                );
            }

            if (memoryUsageMetrics != null && memoryUsageMetrics.size() > 0)
            {
                memoryUsage.add(
                        metricBuilder.build(new MemoryMetricSampleDecorator(new DefaultMetricSample(memoryUsageMetrics)))
                );
            }

            if (memoryTotalMetrics != null && memoryTotalMetrics.size() > 0)
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
        LOGGER.debug("publishing target metrics to ingest api.");
        try
        {
            IngestTargetMetricPostBody targetBody = this.processTargetMetrics(collection);
            Response httpResponse = this.client.postTargetMetrics(targetBody);
            boolean successful = isSuccessStatusCode(httpResponse.getStatusCode()) || (isClientErrorStatusCode(httpResponse.getStatusCode()) && !SUPPORTED_RETRY_CLIENT_ERRORS.contains(httpResponse.getStatusCode()));
            if (successful)
            {
                LOGGER.debug("Published target metrics to Ingest successfully");
            }
            else
            {
                LOGGER.warn("Could not publish target metrics to Ingest. Response HTTP Code: " + httpResponse.getStatusCode());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Response body: " + httpResponse.getResponseBody("UTF-8"));
                }
            }
            return successful;
        }
        catch (Exception e)
        {
            LOGGER.warn("Could not publish target metrics to Ingest, cause: " + e.getMessage());
            LOGGER.debug("Error: ", e);
            return false;
        }
    }

}
