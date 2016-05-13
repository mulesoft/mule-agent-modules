package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestApplicationMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestTargetMetricPostBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Created by sebastianvinci on 5/12/16.
 */
@Singleton
final class MetricTransformer {

    private static final String CPU_METRIC_NAME = "java.lang:type=OperatingSystem:CPU";
    private static final String MEMORY_USAGE_METRIC_NAME = "java.lang:type=Memory:heap used";
    private static final String MEMORY_TOTAL_METRIC_NAME = "java.lang:type=Memory:heap total";

    @Inject
    MetricTransformer()
    {
        super();
    }

    private void processMetric(Metric metric, IngestMetric sample, String expectedName)
    {
        Double value = metric.getValue().doubleValue();
        if (metric.getName().contains(expectedName))
        {
            sample.setCount(sample.getCount() + 1d);
            sample.setSum(sample.getSum() + value);
            if (sample.getMax() == null || sample.getMax() < value)
            {
                sample.setMax(value);
            }
            if (sample.getMin() == null || sample.getMin() > value)
            {
                sample.setMin(value);
            }
        }
    }

    private void processCpuUsage(Metric metric, IngestMetric cpuUsageSample)
    {
        processMetric(metric, cpuUsageSample, CPU_METRIC_NAME);
    }

    private void processMemoryUsage(Metric metric, IngestMetric memoryUsageSample)
    {
        processMetric(metric, memoryUsageSample, MEMORY_USAGE_METRIC_NAME);
    }

    private void processMemoryTotal(Metric metric, IngestMetric memoryTotalSample)
    {
        processMetric(metric, memoryTotalSample, MEMORY_TOTAL_METRIC_NAME);
    }

    private Set<IngestMetric> finishMetric(IngestMetric metric)
    {
        metric.setAvg(metric.getCount() > 0d ? metric.getSum() / metric.getCount() : 0d);
        return Sets.newHashSet(metric);
    }

    IngestTargetMetricPostBody transformTargetMetrics(Collection<List<Metric>> collection)
    {
        Date now = new Date();

        IngestMetric cpuUsageSample = new IngestMetric(now, null, null, 0d, null, 0d);
        IngestMetric memoryUsageSample = new IngestMetric(now, null, null, 0d, null, 0d);
        IngestMetric memoryTotalSample = new IngestMetric(now, null, null, 0d, null, 0d);

        for (List<Metric> metrics : collection)
        {
            for (Metric metric : metrics)
            {
                this.processCpuUsage(metric, cpuUsageSample);
                this.processMemoryTotal(metric, memoryTotalSample);
                this.processMemoryUsage(metric, memoryUsageSample);
            }
        }

        return new IngestTargetMetricPostBody(finishMetric(cpuUsageSample), finishMetric(memoryUsageSample), finishMetric(memoryTotalSample));
    }

    /**
     * TODO This transformation actually just picks target metrics and multiplies/divides them to populate ingest API with data... we don't know yet how to properly get these metrics.
     * */
    IngestApplicationMetricPostBody transformApplicationMetrics(Collection<List<Metric>> collection) {
        Date now = new Date();

        IngestMetric cpuUsageSample = new IngestMetric(now, null, null, 0d, null, 0d);
        IngestMetric memoryUsageSample = new IngestMetric(now, null, null, 0d, null, 0d);
        IngestMetric memoryTotalSample = new IngestMetric(now, null, null, 0d, null, 0d);

        for (List<Metric> metrics : collection)
        {
            for (Metric metric : metrics)
            {
                this.processMemoryUsage(metric, memoryUsageSample);
                this.processCpuUsage(metric, cpuUsageSample);
                this.processMemoryTotal(metric, memoryTotalSample);
            }
        }

        double divider = Math.random() * 1000;
        double min = memoryUsageSample.getMin() == null ? 0 : memoryUsageSample.getMin() * 1000;
        double max = memoryUsageSample.getMax() == null ? 0 : memoryUsageSample.getMax() * 1000;
        double avg = memoryUsageSample.getAvg() == null ? 0 : memoryUsageSample.getAvg() * 1000;
        IngestMetric messagesCount = new IngestMetric(memoryUsageSample.getTime(), min,
                max, memoryUsageSample.getSum() * 1000, avg, memoryUsageSample.getCount());
        IngestMetric errorCount = new IngestMetric(messagesCount.getTime(), messagesCount.getMin() / divider,
                messagesCount.getMax()  / divider, messagesCount.getSum()  / divider, messagesCount.getAvg()  / divider, messagesCount.getCount());

        return new IngestApplicationMetricPostBody(finishMetric(messagesCount), finishMetric(cpuUsageSample), finishMetric(errorCount));
    }

}
