package com.mulesoft.agent.monitoring.publisher.model;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by svinci on 6/28/16.
 */
public class MemoryMetricSampleDecoratorTest
{

    private MetricSampleTestCases testCases = new MetricSampleTestCases();

    @Test
    public void complete()
    {
        MetricSample sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(new Date(), testCases.complete()));
        Assert.assertEquals(0.00000001862645149230957, sample.getMax());
        Assert.assertEquals(0.000000001862645149230957, sample.getMin());
        Assert.assertEquals(0.000000008226682742436727, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(0.00000002468004822731018, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenACoupleOfNulls()
    {
        MetricSample sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(new Date(), testCases.aCoupleOfNulls()));
        Assert.assertEquals(0.00000001862645149230957, sample.getMax());
        Assert.assertEquals(0.000000001862645149230957, sample.getMin());
        Assert.assertEquals(0.000000008226682742436727, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(0.00000002468004822731018, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenNull()
    {
        MetricSample sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(new Date(), null));
        Assert.assertEquals(null, sample.getMax());
        Assert.assertEquals(null, sample.getMin());
        Assert.assertEquals(0d, sample.getAvg());
        Assert.assertEquals(0d, sample.getCount());
        Assert.assertEquals(0d, sample.getSum());
    }

}
