package com.mulesoft.agent.monitoring.publisher.model;

import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.decorator.PercentageMetricSampleDecorator;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by svinci on 6/28/16.
 */
public class PercentageMetricSampleDecoratorTest
{

    private MetricSampleTestCases testCases = new MetricSampleTestCases();

    @Test
    public void complete()
    {
        MetricSample sample = new PercentageMetricSampleDecorator(new DefaultMetricSample(testCases.complete()));
        Assert.assertEquals(2000d, sample.getMax());
        Assert.assertEquals(200d, sample.getMin());
        Assert.assertEquals(883.3333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(2650d, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenACoupleOfNulls()
    {
        MetricSample sample = new PercentageMetricSampleDecorator(new DefaultMetricSample(testCases.aCoupleOfNulls()));
        Assert.assertEquals(2000d, sample.getMax());
        Assert.assertEquals(200d, sample.getMin());
        Assert.assertEquals(883.3333333333334d, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(2650d, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenNull()
    {
        MetricSample sample = new PercentageMetricSampleDecorator(new DefaultMetricSample(null));
        Assert.assertEquals(null, sample.getMax());
        Assert.assertEquals(null, sample.getMin());
        Assert.assertEquals(0d, sample.getAvg());
        Assert.assertEquals(0d, sample.getCount());
        Assert.assertEquals(0d, sample.getSum());
    }

}
