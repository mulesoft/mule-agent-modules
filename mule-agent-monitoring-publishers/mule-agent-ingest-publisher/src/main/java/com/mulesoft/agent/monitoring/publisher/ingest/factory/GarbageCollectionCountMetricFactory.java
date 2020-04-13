package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Factory that calculates the amount of times garbage collection was executed since last read.
 */
@Named("ingest.garbageCollectionCount.metric.factory")
public class GarbageCollectionCountMetricFactory extends TargetMetricFactory
{

    /**
     * Container of metric last reads as garbage collection count is an accumulated metric. Thread safe.
     */
    private Map<SupportedJMXBean, DefaultMetricSample> lastReadMetricContainer = Maps.newConcurrentMap();

    /**
     * {@inheritDoc}
     */
    @Override
    List<SupportedJMXBean> getSupportedMetrics()
    {
        return Lists.newArrayList(SupportedJMXBean.GC_COPY_COUNT, SupportedJMXBean.GC_PAR_NEW_COUNT, SupportedJMXBean.GC_PS_SCAVENGE_COUNT, SupportedJMXBean.GC_G1_YOUNG_GENERATION_COUNT, SupportedJMXBean.GC_MARK_SWEEP_COUNT, SupportedJMXBean.GC_PS_MARK_SWEEP_COUNT, SupportedJMXBean.GC_G1_OLD_GENERATION_COUNT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MetricSample doApply(MetricClassification classification, SupportedJMXBean bean)
    {
        List<Metric> currentReadMetrics = classification.getMetrics(bean.getMetricName());
        DefaultMetricSample currentRead = new DefaultMetricSample(currentReadMetrics);
        DefaultMetricSample lastRead = getLastReadMetric(bean);
        try
        {
            if (lastRead == null)
            {
                return currentRead;
            }
            return new DefaultMetricSample(
                    currentRead.getDate(),
                    currentRead.getMin() - lastRead.getMin(),
                    currentRead.getMax() - lastRead.getMax(),
                    currentRead.getSum() - lastRead.getSum(),
                    currentRead.getAvg() - lastRead.getAvg(),
                    currentRead.getCount()
            );
        }
        finally
        {
            setLastReadMetric(bean, currentRead);
        }
    }

    private void setLastReadMetric(SupportedJMXBean bean, DefaultMetricSample currentRead)
    {
        lastReadMetricContainer.put(bean, currentRead);
    }

    private DefaultMetricSample getLastReadMetric(SupportedJMXBean bean)
    {
        return lastReadMetricContainer.get(bean);
    }
}
