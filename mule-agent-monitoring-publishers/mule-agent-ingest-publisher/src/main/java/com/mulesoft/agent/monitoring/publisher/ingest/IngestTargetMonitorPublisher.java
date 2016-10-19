/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.agent.monitoring.publisher.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.mulesoft.agent.AgentEnableOperationException;
import com.mulesoft.agent.configuration.NotAvailableOn;
import com.mulesoft.agent.configuration.PostConfigure;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.handlers.exception.InitializationException;
import com.mulesoft.agent.monitoring.publisher.ingest.factory.TargetMetricFactory;
import com.mulesoft.agent.monitoring.publisher.ingest.model.JMXMetricFieldMapping;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestMetric;
import com.mulesoft.agent.services.OnOffSwitch;
import com.ning.http.client.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    /**
     * All instances of TargetMetricFactory.
     */
    @Inject
    private List<TargetMetricFactory> targetMetricFactories;

    /**
     * Initialization code to be run after configuration.
     * @throws InitializationException when target metric factories we not injected or when configuration is invalid.
     */
    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();
        if (targetMetricFactories == null)
        {
            throw new InitializationException("Target metric factories weren't injected!");
        }
    }

    /**
     * Process the buffer's contents and build the bodies to be posted to Ingest API.
     *
     * @param collection Buffer contents.
     * @return Processed target metrics ready to be sent to ingest API.
     */
    private Map<String, Set<IngestMetric>> processTargetMetrics(Collection<List<Metric>> collection)
    {

        Map<String, Set<IngestMetric>> result = Maps.newHashMap();

        for (List<Metric> sample : collection)
        {
            List<String> keys = Lists.newLinkedList();
            for (SupportedJMXBean bean : SupportedJMXBean.values())
            {
                keys.add(bean.getMetricName());
            }

            MetricClassification classification = new MetricClassification(keys, sample);

            for (SupportedJMXBean bean : SupportedJMXBean.values())
            {

                TargetMetricFactory applicableFactory = null;
                for (TargetMetricFactory factory : targetMetricFactories)
                {
                    if (factory.appliesForMetric(bean))
                    {
                        applicableFactory = factory;
                    }
                }
                if (applicableFactory == null)
                {
                    LOGGER.debug("no factory found for bean " + bean.name() + " in array of " + targetMetricFactories.size() + " factories.");
                    return null;
                }

                IngestMetric metric = applicableFactory.apply(classification, bean);
                if (metric != null && metric.getCount() > 0)
                {
                    JMXMetricFieldMapping mapping = JMXMetricFieldMapping.forSupportedJMXBean(bean);
                    Set<IngestMetric> existentMetrics = result.get(mapping.getFieldName());
                    if (existentMetrics != null)
                    {
                        existentMetrics.add(metric);
                    }
                    else
                    {
                        result.put(mapping.getFieldName(), Sets.newHashSet(metric));
                    }
                }
            }

        }

        return result;
    }

    /**
     * Grab and process the contents of the buffer and send them to Ingest API.
     *
     * @param collection Buffer contents.
     * @return Whether the run was successful or not.
     */
    protected boolean send(Collection<List<Metric>> collection)
    {
        LOGGER.info("publishing target metrics to ingest api.");
        try
        {
            Map<String, Set<IngestMetric>> targetBody = this.processTargetMetrics(collection);
            if (targetBody != null)
            {
                LOGGER.debug(targetBody.toString());
            }

            Response httpResponse = this.client.postTargetMetrics(targetBody);
            boolean successful = isSuccessStatusCode(httpResponse.getStatusCode());
            if (successful)
            {
                LOGGER.info("Published target metrics to Ingest successfully");
            }
            else
            {
                LOGGER.warn("Could not publish target metrics to Ingest.");
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
