package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetricPostBody;
import com.mulesoft.agent.monitoring.publisher.ingest.model.IngestMetric;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by sebastianvinci on 5/13/16.
 */
public class MetricTransformerTest
{

    @Test
    public void shouldBehaveCorrectlyOnCollectionOfEmptyLists()
    {
        Collection<List<Metric>> collection = new LinkedList<>();
        for (int i = 0; i < 1000; i++)
        {
            collection.add(new LinkedList<Metric>());
        }

        IngestMetricPostBody result = new MetricTransformer().transform(collection);

        Assert.assertEquals(1, result.getCpuUsage().size());
        Assert.assertEquals(1, result.getMemoryUsage().size());
        Assert.assertEquals(1, result.getMemoryTotal().size());

        IngestMetric cpu = result.getCpuUsage().iterator().next();
        Assert.assertEquals(null, cpu.getMin());
        Assert.assertEquals(null, cpu.getMax());
        Assert.assertEquals(new Double(0), cpu.getSum());
        Assert.assertEquals(new Double(0), cpu.getAvg());
        Assert.assertEquals(new Double(0d), cpu.getCount());

        IngestMetric memoryUsage = result.getMemoryUsage().iterator().next();
        Assert.assertEquals(null, memoryUsage.getMin());
        Assert.assertEquals(null, memoryUsage.getMax());
        Assert.assertEquals(new Double(0), memoryUsage.getSum());
        Assert.assertEquals(new Double(0), memoryUsage.getAvg());
        Assert.assertEquals(new Double(0d), memoryUsage.getCount());

        IngestMetric memoryTotal = result.getMemoryTotal().iterator().next();
        Assert.assertEquals(null, memoryTotal.getMin());
        Assert.assertEquals(null, memoryTotal.getMax());
        Assert.assertEquals(new Double(0), memoryTotal.getSum());
        Assert.assertEquals(new Double(0), memoryTotal.getAvg());
        Assert.assertEquals(new Double(0d), memoryTotal.getCount());
    }

    @Test
    public void shouldBehaveCorrectlyOnEmptyCollection()
    {
        Collection<List<Metric>> collection = new LinkedList<>();

        IngestMetricPostBody result = new MetricTransformer().transform(collection);

        Assert.assertEquals(1, result.getCpuUsage().size());
        Assert.assertEquals(1, result.getMemoryUsage().size());
        Assert.assertEquals(1, result.getMemoryTotal().size());

        IngestMetric cpu = result.getCpuUsage().iterator().next();
        Assert.assertEquals(null, cpu.getMin());
        Assert.assertEquals(null, cpu.getMax());
        Assert.assertEquals(new Double(0), cpu.getSum());
        Assert.assertEquals(new Double(0), cpu.getAvg());
        Assert.assertEquals(new Double(0d), cpu.getCount());

        IngestMetric memoryUsage = result.getMemoryUsage().iterator().next();
        Assert.assertEquals(null, memoryUsage.getMin());
        Assert.assertEquals(null, memoryUsage.getMax());
        Assert.assertEquals(new Double(0), memoryUsage.getSum());
        Assert.assertEquals(new Double(0d), memoryUsage.getAvg());
        Assert.assertEquals(new Double(0d), memoryUsage.getCount());

        IngestMetric memoryTotal = result.getMemoryTotal().iterator().next();
        Assert.assertEquals(null, memoryTotal.getMin());
        Assert.assertEquals(null, memoryTotal.getMax());
        Assert.assertEquals(new Double(0), memoryTotal.getSum());
        Assert.assertEquals(new Double(0d), memoryTotal.getAvg());
        Assert.assertEquals(new Double(0d), memoryTotal.getCount());
    }

    @Test
    public void filtersNonDesiredMetrics()
    {
        Collection<List<Metric>> collection = completeTestCase();

        IngestMetricPostBody result = new MetricTransformer().transform(collection);
        Assert.assertEquals(1, result.getCpuUsage().size());
        Assert.assertEquals(new Double(4), result.getCpuUsage().iterator().next().getCount());

        Assert.assertEquals(1, result.getMemoryUsage().size());
        Assert.assertEquals(new Double(6), result.getMemoryUsage().iterator().next().getCount());

        Assert.assertEquals(1, result.getMemoryTotal().size());
        Assert.assertEquals(new Double(5), result.getMemoryTotal().iterator().next().getCount());
    }

    @Test
    public void properlyCalculatesMetrics()
    {
        Collection<List<Metric>> collection = completeTestCase();

        IngestMetricPostBody result = new MetricTransformer().transform(collection);

        IngestMetric cpu = result.getCpuUsage().iterator().next();
        Assert.assertEquals(new Double(0.1d), cpu.getMin());
        Assert.assertEquals(new Double(8.1d), cpu.getMax());
        Assert.assertEquals(new Double(16.8d), cpu.getSum());
        Assert.assertEquals(new Double(4.2d), cpu.getAvg());
        Assert.assertEquals(new Double(4d), cpu.getCount());

        IngestMetric memoryUsage = result.getMemoryUsage().iterator().next();
        Assert.assertEquals(new Double(0.7d), memoryUsage.getMin());
        Assert.assertEquals(new Double(10d), memoryUsage.getMax());
        Assert.assertEquals(new Double(35.5d), memoryUsage.getSum());
        Assert.assertEquals(new Double(5.916666666666667d), memoryUsage.getAvg());
        Assert.assertEquals(new Double(6d), memoryUsage.getCount());

        IngestMetric memoryTotal = result.getMemoryTotal().iterator().next();
        Assert.assertEquals(new Double(3.9d), memoryTotal.getMin());
        Assert.assertEquals(new Double(15.3d), memoryTotal.getMax());
        Assert.assertEquals(new Double(42.22d), memoryTotal.getSum());
        Assert.assertEquals(new Double(8.443999999999999d), memoryTotal.getAvg());
        Assert.assertEquals(new Double(5d), memoryTotal.getCount());
    }

    private Collection<List<Metric>> completeTestCase()
    {
        List<Metric> elements1 = Lists.newArrayList(
                new Metric(new Date().getTime(), "java.lang:type=OperatingSystem:CPU", 3.4d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap total", 3.9d),
                new Metric(new Date().getTime(), "this metric should be left out", 1d),
                new Metric(new Date().getTime(), "java.lang:type=OperatingSystem:CPU", 8.1d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap used", 9.2d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap total", 9.9d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap used", 0.7d),
                new Metric(new Date().getTime(), "java.lang:type=OperatingSystem:CPU", 5.2d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap total", 15.3d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap used", 2.9d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap total", 5d)
        );
        List<Metric> elements2 = Lists.newArrayList(
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap used", 5d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap total", 8.12d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap used", 7.7d),
                new Metric(new Date().getTime(), "java.lang:type=Memory:heap used", 10d),
                new Metric(new Date().getTime(), "this metric should be left out", 1d),
                new Metric(new Date().getTime(), "java.lang:type=OperatingSystem:CPU", 0.1d)
                );
        return Lists.newArrayList(elements1, elements2);
    }

}
