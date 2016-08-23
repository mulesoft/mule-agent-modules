/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mulesoft.agent.configuration.NotAvailableOn;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestTargetMetricPostBodyBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.model.MemoryMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.model.PercentageMetricSampleDecorator;
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

    @Inject
    private IngestTargetMetricPostBodyBuilder targetMetricBuilder;

    private void processPercentageMetric(Collection<IngestMetric> buffer, MetricClassification classification, SupportedJMXBean bean) {
        List<Metric> metrics = classification.getMetrics(bean.getMetricName());
        if (metrics != null && metrics.size() > 0) {
            buffer.add(metricBuilder.build(new PercentageMetricSampleDecorator(new DefaultMetricSample(metrics))));
        }
    }

    private void processMemoryMetric(Collection<IngestMetric> buffer, MetricClassification classification, SupportedJMXBean bean) {
        List<Metric> metrics = classification.getMetrics(bean.getMetricName());
        if (metrics != null && metrics.size() > 0) {
            buffer.add(metricBuilder.build(new MemoryMetricSampleDecorator(new DefaultMetricSample(metrics))));
        }
    }

    private IngestTargetMetricPostBody processTargetMetrics(Collection<List<Metric>> collection)
    {

        Set<IngestMetric> cpuUsage = Sets.newHashSet();

        Set<IngestMetric> heapUsage = Sets.newHashSet();
        Set<IngestMetric> heapCommitted = Sets.newHashSet();
        Set<IngestMetric> heapTotal = Sets.newHashSet();

        for (List<Metric> sample : collection)
        {
            List<String> keys = Lists.newLinkedList();
            for (SupportedJMXBean bean : Arrays.asList(SupportedJMXBean.values())) {
                keys.add(bean.getMetricName());
            }

            MetricClassification classification = new MetricClassification(keys, sample);

            this.processPercentageMetric(cpuUsage, classification, SupportedJMXBean.CPU_USAGE);

            this.processMemoryMetric(heapUsage, classification, SupportedJMXBean.HEAP_USAGE);
            this.processMemoryMetric(heapCommitted, classification, SupportedJMXBean.HEAP_COMMITTED);
            this.processMemoryMetric(heapTotal, classification, SupportedJMXBean.HEAP_TOTAL);
        }

        return targetMetricBuilder.build(cpuUsage, heapUsage, heapTotal);
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
