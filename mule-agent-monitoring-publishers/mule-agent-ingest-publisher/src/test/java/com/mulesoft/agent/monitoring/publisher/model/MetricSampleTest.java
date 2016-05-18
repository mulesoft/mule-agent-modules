package com.mulesoft.agent.monitoring.publisher.model;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;

public class MetricSampleTest
{

    private MetricSampleTestCases testCases = new MetricSampleTestCases();

    @Test
    public void complete()
    {
        MetricSample sample = new MetricSample(new Date(), testCases.complete());
        Assert.assertEquals(20d, sample.getMax());
        Assert.assertEquals(2d, sample.getMin());
        Assert.assertEquals(8.833333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(26.5, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenACoupleOfNulls()
    {
        MetricSample sample = new MetricSample(new Date(), testCases.aCoupleOfNulls());
        Assert.assertEquals(20d, sample.getMax());
        Assert.assertEquals(2d, sample.getMin());
        Assert.assertEquals(8.833333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(26.5, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenNull()
    {
        MetricSample sample = new MetricSample(new Date(), null);
        Assert.assertEquals(null, sample.getMax());
        Assert.assertEquals(null, sample.getMin());
        Assert.assertEquals(0d, sample.getAvg());
        Assert.assertEquals(0d, sample.getCount());
        Assert.assertEquals(0d, sample.getSum());
    }

}
