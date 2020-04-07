/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.decorator.PercentageMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

import javax.inject.Named;
import java.util.List;

/**
 * IngestMetric factory that returns an IngestMetric with its values converted to percentages. (0.9 to 90).
 */
@Named("ingest.percentage.metric.factory")
public class PercentageMetricFactory extends TargetMetricFactory
{

    private static final List<SupportedJMXBean> SUPPORTED_METRICS = Lists.newArrayList(SupportedJMXBean.CPU_USAGE);

    /**
     * {@inheritDoc}
     */
    @Override
    List<SupportedJMXBean> getSupportedMetrics()
    {
        return SUPPORTED_METRICS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MetricSample doApply(MetricClassification classification, SupportedJMXBean bean)
    {
        List<Metric> metrics = classification.getMetrics(bean.getMetricName());
        return new PercentageMetricSampleDecorator(new DefaultMetricSample(metrics));
    }
}
