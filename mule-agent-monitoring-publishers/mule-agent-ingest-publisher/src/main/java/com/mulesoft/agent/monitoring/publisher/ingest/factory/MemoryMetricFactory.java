package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.decorator.MemoryMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;
import jersey.repackaged.com.google.common.collect.Lists;

import javax.inject.Named;
import java.util.List;

/**
 * IngestMetric factory that returns an IngestMetric with its values converted from bytes to mega bytes.
 */
@Named("factory.ingest.metric.memory")
public class MemoryMetricFactory extends TargetMetricFactory {

    private static final java.util.ArrayList<SupportedJMXBean> SUPPORTED_METRICS = Lists.newArrayList(
        SupportedJMXBean.CODE_CACHE_COMMITTED, SupportedJMXBean.CODE_CACHE_TOTAL, SupportedJMXBean.CODE_CACHE_USAGE,
        SupportedJMXBean.COMPRESSED_CLASS_SPACE_COMMITTED, SupportedJMXBean.COMPRESSED_CLASS_SPACE_TOTAL,
        SupportedJMXBean.COMPRESSED_CLASS_SPACE_USAGE, SupportedJMXBean.EDEN_COMMITTED, SupportedJMXBean.EDEN_TOTAL,
        SupportedJMXBean.EDEN_USAGE, SupportedJMXBean.HEAP_COMMITTED, SupportedJMXBean.HEAP_TOTAL, SupportedJMXBean.HEAP_USAGE,
        SupportedJMXBean.METASPACE_COMMITTED, SupportedJMXBean.METASPACE_USAGE, SupportedJMXBean.OLD_GEN_COMMITTED,
        SupportedJMXBean.OLD_GEN_TOTAL, SupportedJMXBean.OLD_GEN_USAGE, SupportedJMXBean.SURVIVOR_COMMITTED,
        SupportedJMXBean.SURVIVOR_TOTAL, SupportedJMXBean.SURVIVOR_USAGE, SupportedJMXBean.METASPACE_TOTAL
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
        MemoryMetricSampleDecorator sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(metrics));
        // Java 8 included meta space as a new native memory location, which max value, by default, is set to the
        // total physical memory of the host.
        // Anyway, in that case, JMX reports that value as -1, so this case here checks if -1 was reported to call a fallback.
        if (bean == SupportedJMXBean.METASPACE_TOTAL && sample.getAvg() < 0) {
            return metaspaceFallback(classification);
        }
        return sample;
    }

    /**
     * Fallback to be used when meta space total is reported as -1, to set meta space total to the total physical memory of the host.
     * @param classification MetricClassification from which to extract the total physical memory metrics.
     * @return MetricSample resolved from total physical memory.
     */
    private MetricSample metaspaceFallback(MetricClassification classification) {
        List<Metric> metrics = classification.getMetrics(SupportedJMXBean.TOTAL_PHYSICAL_MEMORY.getMetricName());
        return new MemoryMetricSampleDecorator(new DefaultMetricSample(metrics));
    }
}
