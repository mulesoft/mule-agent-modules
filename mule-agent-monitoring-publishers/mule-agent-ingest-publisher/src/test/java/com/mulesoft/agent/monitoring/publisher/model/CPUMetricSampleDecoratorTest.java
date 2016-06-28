package com.mulesoft.agent.monitoring.publisher.model;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by svinci on 6/28/16.
 */
public class CPUMetricSampleDecoratorTest
{

    private MetricSampleTestCases testCases = new MetricSampleTestCases();

    @Test
    public void complete()
    {
        MetricSample sample = new CPUMetricSampleDecorator(new DefaultMetricSample(new Date(), testCases.complete()));
        Assert.assertEquals(2000d, sample.getMax());
        Assert.assertEquals(200d, sample.getMin());
        Assert.assertEquals(883.3333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(2650d, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenACoupleOfNulls()
    {
        MetricSample sample = new CPUMetricSampleDecorator(new DefaultMetricSample(new Date(), testCases.aCoupleOfNulls()));
        Assert.assertEquals(2000d, sample.getMax());
        Assert.assertEquals(200d, sample.getMin());
        Assert.assertEquals(883.3333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(2650d, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenNull()
    {
        MetricSample sample = new CPUMetricSampleDecorator(new DefaultMetricSample(new Date(), null));
        Assert.assertEquals(null, sample.getMax());
        Assert.assertEquals(null, sample.getMin());
        Assert.assertEquals(0d, sample.getAvg());
        Assert.assertEquals(0d, sample.getCount());
        Assert.assertEquals(0d, sample.getSum());
    }

}
