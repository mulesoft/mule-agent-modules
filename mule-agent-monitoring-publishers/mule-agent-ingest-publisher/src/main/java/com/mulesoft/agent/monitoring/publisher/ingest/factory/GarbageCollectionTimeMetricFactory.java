package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestMetricBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.decorator.PercentageMetricSampleDecorator;
import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * IngestMetric factory that calculates the percentage of time taken by garbage collection.
 */
@Named("ingest.garbageCollectionTime.metric.factory")
public class GarbageCollectionTimeMetricFactory extends TargetMetricFactory
{

    /**
     * Container of metric last reads as garbage collection time is an accumulated metric. Thread safe.
     */
    private Map<SupportedJMXBean, DefaultMetricSample> lastReadMetricContainer = Maps.newConcurrentMap();

    /**
     * Container of uptime last reads as jvm uptime is an accumulated metric. Thread safe.
     */
    private Map<SupportedJMXBean, Double> lastReadUptimeContainer = Maps.newConcurrentMap();

    public GarbageCollectionTimeMetricFactory()
    {
    }

    public GarbageCollectionTimeMetricFactory(IngestMetricBuilder metricBuilder)
    {
        super(metricBuilder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    List<SupportedJMXBean> getSupportedMetrics()
    {
        return Lists.newArrayList(SupportedJMXBean.GC_MARK_SWEEP_TIME, SupportedJMXBean.GC_PAR_NEW_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MetricSample doApply(MetricClassification classification, SupportedJMXBean bean)
    {
        Double currentUptime = this.getCurrentUptime(classification);
        Double lastUptime = this.getLastReadUptime(bean);

        List<Metric> currentReadMetrics = classification.getMetrics(bean.getMetricName());
        DefaultMetricSample currentRead = new DefaultMetricSample(currentReadMetrics);
        DefaultMetricSample lastRead = getLastReadMetric(bean);

        try
        {
            if (lastRead == null || lastUptime == null || lastUptime.equals(currentUptime))
            {
                return calculate(currentRead.getDate(), currentRead.getMin(), currentRead.getMax(),
                        currentRead.getSum(), currentRead.getAvg(), currentRead.getCount(), currentUptime);
            }

            Double period = currentUptime - lastUptime;
            return calculate(currentRead.getDate(), currentRead.getMin() - lastRead.getMin(), currentRead.getMax() - lastRead.getMax(),
                    currentRead.getSum() - lastRead.getSum(), currentRead.getAvg() - lastRead.getAvg(), currentRead.getCount(), period);
        }
        finally
        {
            setLastReadMetric(bean, currentRead);
            setLastReadUptime(bean, currentUptime);
        }
    }

    private MetricSample calculate(Date date, Double min, Double max, Double sum, Double avg, Double count, Double period)
    {
        Preconditions.checkArgument(period > 0, "Period between last read and now should never be less than zero.");
        return build(date, min / period, max / period, sum / period, avg / period, count);
    }

    private MetricSample build(Date date, Double min, Double max, Double sum, Double avg, Double count)
    {
        return new PercentageMetricSampleDecorator(
            new DefaultMetricSample(
                date,
                min > 0 ? min : 0,
                max > 0 ? max : 0,
                sum > 0 ? sum : 0,
                avg > 0 ? avg : 0,
                count
            )
        );
    }

    private void setLastReadMetric(SupportedJMXBean bean, DefaultMetricSample currentRead)
    {
        lastReadMetricContainer.put(bean, currentRead);
    }

    private DefaultMetricSample getLastReadMetric(SupportedJMXBean bean)
    {
        return lastReadMetricContainer.get(bean);
    }

    private Double getCurrentUptime(MetricClassification classification)
    {
        List<Metric> uptimeMetrics = classification.getMetrics(SupportedJMXBean.JVM_UPTIME.getMetricName());
        DefaultMetricSample uptimeMetric = new DefaultMetricSample(uptimeMetrics);
        return uptimeMetric.getAvg();
    }

    private void setLastReadUptime(SupportedJMXBean bean, Double currentUptime)
    {
        lastReadUptimeContainer.put(bean, currentUptime);
    }

    private Double getLastReadUptime(SupportedJMXBean bean)
    {
        return lastReadUptimeContainer.get(bean);
    }

}
