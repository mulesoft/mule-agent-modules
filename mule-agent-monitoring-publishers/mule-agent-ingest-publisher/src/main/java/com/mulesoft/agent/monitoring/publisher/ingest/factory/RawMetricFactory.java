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
@Named("factory.ingest.metric.raw")
public class RawMetricFactory extends TargetMetricFactory {

    private static final java.util.ArrayList<SupportedJMXBean> SUPPORTED_METRICS = Lists.newArrayList(
        SupportedJMXBean.AVAILABLE_PROCESSORS, SupportedJMXBean.CLASS_LOADING_LOADED,
        SupportedJMXBean.CLASS_LOADING_TOTAL, SupportedJMXBean.CLASS_LOADING_UNLOADED,
        SupportedJMXBean.GC_MARK_SWEEP_COUNT, SupportedJMXBean.GC_MARK_SWEEP_TIME, SupportedJMXBean.GC_SCAVENGE_COUNT,
        SupportedJMXBean.GC_SCAVENGE_TIME, SupportedJMXBean.LOAD_AVERAGE, SupportedJMXBean.THREADING_COUNT
    );

    /**
     * {@inheritDoc}
     */
    @Override
    List<SupportedJMXBean> getSupportedMetrics() {
        return SUPPORTED_METRICS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MetricSample doApply(MetricClassification classification, SupportedJMXBean bean) {
        List<Metric> metrics = classification.getMetrics(bean.getMetricName());
        return new DefaultMetricSample(metrics);
    }
}
