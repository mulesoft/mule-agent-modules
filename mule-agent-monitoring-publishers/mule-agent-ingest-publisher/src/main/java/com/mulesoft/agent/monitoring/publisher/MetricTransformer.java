package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Sets;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.model.IdPOSTBody;
import com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.model.IngestMetric;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by sebastianvinci on 5/12/16.
 */
final class MetricTransformer {

    private static final String CPU_METRIC_NAME = "java.lang:type=OperatingSystem:CPU";
    private static final String MEMORY_USAGE_METRIC_NAME = "java.lang:type=Memory:heap used";
    private static final String MEMORY_TOTAL_METRIC_NAME = "java.lang:type=Memory:heap total";

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

    IdPOSTBody transform(Collection<List<Metric>> collection)
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
                this.processMemoryUsage(metric, memoryUsageSample);
                this.processMemoryTotal(metric, memoryTotalSample);
            }
        }

        return new IdPOSTBody(finishMetric(cpuUsageSample), finishMetric(memoryUsageSample), finishMetric(memoryTotalSample));
    }

}
