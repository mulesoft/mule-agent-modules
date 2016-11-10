/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 *
 */

package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

import javax.inject.Named;
import java.util.List;

/**
 * IngestMetric factory that returns an IngestMetric without any conversion to its values.
 */
@Named("ingest.raw.metric.factory")
public class RawMetricFactory extends TargetMetricFactory
{

    private static final List<SupportedJMXBean> SUPPORTED_METRICS = Lists.newArrayList(
        SupportedJMXBean.AVAILABLE_PROCESSORS, SupportedJMXBean.CLASS_LOADING_LOADED,
        SupportedJMXBean.CLASS_LOADING_TOTAL, SupportedJMXBean.CLASS_LOADING_UNLOADED,
        SupportedJMXBean.LOAD_AVERAGE, SupportedJMXBean.THREADING_COUNT
    );

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
        return new DefaultMetricSample(metrics);
    }
}
