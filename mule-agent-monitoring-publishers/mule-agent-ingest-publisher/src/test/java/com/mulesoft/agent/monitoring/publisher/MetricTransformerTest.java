package com.mulesoft.agent.monitoring.publisher;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.model.IdPOSTBody;
import com.mulesoft.agent.monitoring.publisher.api.resource.targets.id.model.IngestMetric;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by sebastianvinci on 5/13/16.
 */
public class MetricTransformerTest {

    @Test
    public void shouldBehaveCorrectlyOnCollectionOfEmptyLists() {
        Collection<List<Metric>> collection = new LinkedList<>();
        for (int i = 0; i < 1000; i++)
            collection.add(new LinkedList<Metric>());

        IdPOSTBody result = new MetricTransformer().transform(collection);

        Assert.assertEquals(result.getCpuUsage().size(), 1);

        Assert.assertEquals(result.getMemoryUsage().size(), 1);

        Assert.assertEquals(result.getMemoryTotal().size(), 1);

        IngestMetric cpu = result.getCpuUsage().iterator().next();
        Assert.assertEquals(cpu.getMin(), null);
        Assert.assertEquals(cpu.getMax(), null);
        Assert.assertEquals(cpu.getSum(), new Double(0));
        Assert.assertEquals(cpu.getAvg(), new Double(0));
        Assert.assertEquals(cpu.getCount(), new Double(0d));

        IngestMetric memoryUsage = result.getMemoryUsage().iterator().next();
        Assert.assertEquals(memoryUsage.getMin(), null);
        Assert.assertEquals(memoryUsage.getMax(), null);
        Assert.assertEquals(memoryUsage.getSum(), new Double(0));
        Assert.assertEquals(memoryUsage.getAvg(), new Double(0));
        Assert.assertEquals(memoryUsage.getCount(), new Double(0d));

        IngestMetric memoryTotal = result.getMemoryTotal().iterator().next();
        Assert.assertEquals(memoryTotal.getMin(), null);
        Assert.assertEquals(memoryTotal.getMax(), null);
        Assert.assertEquals(memoryTotal.getSum(), new Double(0));
        Assert.assertEquals(memoryTotal.getAvg(), new Double(0));
        Assert.assertEquals(memoryTotal.getCount(), new Double(0d));
    }

    @Test
    public void shouldBehaveCorrectlyOnEmptyCollection() {
        Collection<List<Metric>> collection = new LinkedList<>();

        IdPOSTBody result = new MetricTransformer().transform(collection);

        Assert.assertEquals(result.getCpuUsage().size(), 1);

        Assert.assertEquals(result.getMemoryUsage().size(), 1);

        Assert.assertEquals(result.getMemoryTotal().size(), 1);

        IngestMetric cpu = result.getCpuUsage().iterator().next();
        Assert.assertEquals(cpu.getMin(), null);
        Assert.assertEquals(cpu.getMax(), null);
        Assert.assertEquals(cpu.getSum(), new Double(0));
        Assert.assertEquals(cpu.getAvg(), new Double(0));
        Assert.assertEquals(cpu.getCount(), new Double(0d));

        IngestMetric memoryUsage = result.getMemoryUsage().iterator().next();
        Assert.assertEquals(memoryUsage.getMin(), null);
        Assert.assertEquals(memoryUsage.getMax(), null);
        Assert.assertEquals(memoryUsage.getSum(), new Double(0));
        Assert.assertEquals(memoryUsage.getAvg(), new Double(0));
        Assert.assertEquals(memoryUsage.getCount(), new Double(0d));

        IngestMetric memoryTotal = result.getMemoryTotal().iterator().next();
        Assert.assertEquals(memoryTotal.getMin(), null);
        Assert.assertEquals(memoryTotal.getMax(), null);
        Assert.assertEquals(memoryTotal.getSum(), new Double(0));
        Assert.assertEquals(memoryTotal.getAvg(), new Double(0));
        Assert.assertEquals(memoryTotal.getCount(), new Double(0d));
    }

    @Test
    public void filtersNonDesiredMetrics()
    {
        Collection<List<Metric>> collection = completeTestCase();

        IdPOSTBody result = new MetricTransformer().transform(collection);
        Assert.assertEquals(result.getCpuUsage().size(), 1);
        Assert.assertEquals(result.getCpuUsage().iterator().next().getCount(), new Double(4));

        Assert.assertEquals(result.getMemoryUsage().size(), 1);
        Assert.assertEquals(result.getMemoryUsage().iterator().next().getCount(), new Double(6));

        Assert.assertEquals(result.getMemoryTotal().size(), 1);
        Assert.assertEquals(result.getMemoryTotal().iterator().next().getCount(), new Double(5));
    }

    @Test
    public void properlyCalculatesMetrics() {
        Collection<List<Metric>> collection = completeTestCase();

        IdPOSTBody result = new MetricTransformer().transform(collection);

        IngestMetric cpu = result.getCpuUsage().iterator().next();
        Assert.assertEquals(cpu.getMin(), new Double(0.1d));
        Assert.assertEquals(cpu.getMax(), new Double(8.1d));
        Assert.assertEquals(cpu.getSum(), new Double(16.8d));
        Assert.assertEquals(cpu.getAvg(), new Double(4.2d));
        Assert.assertEquals(cpu.getCount(), new Double(4d));

        IngestMetric memoryUsage = result.getMemoryUsage().iterator().next();
        Assert.assertEquals(memoryUsage.getMin(), new Double(0.7d));
        Assert.assertEquals(memoryUsage.getMax(), new Double(10d));
        Assert.assertEquals(memoryUsage.getSum(), new Double(35.5d));
        Assert.assertEquals(memoryUsage.getAvg(), new Double(5.916666666666667d));
        Assert.assertEquals(memoryUsage.getCount(), new Double(6d));

        IngestMetric memoryTotal = result.getMemoryTotal().iterator().next();
        Assert.assertEquals(memoryTotal.getMin(), new Double(3.9d));
        Assert.assertEquals(memoryTotal.getMax(), new Double(15.3d));
        Assert.assertEquals(memoryTotal.getSum(), new Double(42.22d));
        Assert.assertEquals(memoryTotal.getAvg(), new Double(8.443999999999999d));
        Assert.assertEquals(memoryTotal.getCount(), new Double(5d));
    }

    private Collection<List<Metric>> completeTestCase() {
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
