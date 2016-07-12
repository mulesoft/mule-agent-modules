package com.mulesoft.agent.monitoring.publisher.model;

import junit.framework.Assert;
import org.junit.Test;

public class DefaultMetricSampleTest
{

    private MetricSampleTestCases testCases = new MetricSampleTestCases();

    @Test
    public void complete()
    {
        DefaultMetricSample sample = new DefaultMetricSample(testCases.complete());
        Assert.assertEquals(20d, sample.getMax());
        Assert.assertEquals(2d, sample.getMin());
        Assert.assertEquals(8.833333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(26.5, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenACoupleOfNulls()
    {
        DefaultMetricSample sample = new DefaultMetricSample(testCases.aCoupleOfNulls());
        Assert.assertEquals(20d, sample.getMax());
        Assert.assertEquals(2d, sample.getMin());
        Assert.assertEquals(8.833333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(26.5, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenNull()
    {
        DefaultMetricSample sample = new DefaultMetricSample(null);
        Assert.assertEquals(null, sample.getMax());
        Assert.assertEquals(null, sample.getMin());
        Assert.assertEquals(0d, sample.getAvg());
        Assert.assertEquals(0d, sample.getCount());
        Assert.assertEquals(0d, sample.getSum());
    }

}
