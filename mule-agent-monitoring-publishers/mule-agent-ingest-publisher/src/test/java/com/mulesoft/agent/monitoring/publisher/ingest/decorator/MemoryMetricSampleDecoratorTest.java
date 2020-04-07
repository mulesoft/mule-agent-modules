/**
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */

package com.mulesoft.agent.monitoring.publisher.ingest.decorator;

import com.mulesoft.agent.monitoring.publisher.ingest.model.DefaultMetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.MetricSampleTestCases;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by svinci on 6/28/16.
 */
public class MemoryMetricSampleDecoratorTest
{

    private MetricSampleTestCases testCases = new MetricSampleTestCases();

    @Test
    public void complete()
    {
        MetricSample sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(testCases.complete()));
        Assert.assertEquals(0.000019073486328125, sample.getMax());
        Assert.assertEquals(0.0000019073486328125, sample.getMin());
        Assert.assertEquals(0.000008424123128255209, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(0.000025272369384765625, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenACoupleOfNulls()
    {
        MetricSample sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(testCases.aCoupleOfNulls()));
        Assert.assertEquals(0.000019073486328125, sample.getMax());
        Assert.assertEquals(0.0000019073486328125, sample.getMin());
        Assert.assertEquals(0.000008424123128255209, sample.getAvg());
        Assert.assertEquals(3d, sample.getCount());
        Assert.assertEquals(0.000025272369384765625, sample.getSum());
    }

    @Test
    public void shouldNotThrowNPEWhenGivenNull()
    {
        MetricSample sample = new MemoryMetricSampleDecorator(new DefaultMetricSample(null));
        Assert.assertEquals(null, sample.getMax());
        Assert.assertEquals(null, sample.getMin());
        Assert.assertEquals(0d, sample.getAvg());
        Assert.assertEquals(0d, sample.getCount());
        Assert.assertEquals(0d, sample.getSum());
    }

}
