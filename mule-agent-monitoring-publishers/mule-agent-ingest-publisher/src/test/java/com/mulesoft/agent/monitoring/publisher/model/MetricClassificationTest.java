package com.mulesoft.agent.monitoring.publisher.model;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class MetricClassificationTest {

    private MetricClassificationTestCases metricClassificationTestCases = new MetricClassificationTestCases();
    private List<String> keys = Lists.newArrayList(MetricClassificationTestCases.CPU_USAGE_NAME, MetricClassificationTestCases.MEMORY_TOTAL_NAME, MetricClassificationTestCases.MEMORY_USAGE_NAME);

    @Test
    public void shouldNotThrowNPEWhenIPassACoupleNullsToIt()
    {
        Collection<List<Metric>> collection = metricClassificationTestCases.someNullsTestCase();
        new MetricClassification(keys, collection);
    }

    @Test
    public void shouldNotThrowNPEWhenIPassNullToIt()
    {
        new MetricClassification(null, null);
    }

    @Test
    public void shouldNotThrowNPEWhenKeysIsNull()
    {
        new MetricClassification(null, metricClassificationTestCases.completeTestCase());
    }

    @Test
    public void shouldNotThrowNPEWhenMetricsIsNull()
    {
        new MetricClassification(keys, null);
    }

    @Test
    public void shouldBeEmptyWhenKeysIsEmpty()
    {
        MetricClassification classification = new MetricClassification(Lists.<String>newLinkedList(), metricClassificationTestCases.completeTestCase());
        Map<String, List<Metric>> map = classification.getClassification();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void shouldBeEmptyWhenCollectionOfEmptyListsIsGiven()
    {
        MetricClassification classification = new MetricClassification(this.keys, metricClassificationTestCases.collectionOfEmptyLists());
        Map<String, List<Metric>> map = classification.getClassification();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void happyCase()
    {
        MetricClassification classification = new MetricClassification(this.keys, metricClassificationTestCases.completeTestCase());
        Assert.assertEquals(5, classification.getMetrics(MetricClassificationTestCases.MEMORY_TOTAL_NAME).size());
        Assert.assertEquals(6, classification.getMetrics(MetricClassificationTestCases.MEMORY_USAGE_NAME).size());
        Assert.assertEquals(4, classification.getMetrics(MetricClassificationTestCases.CPU_USAGE_NAME).size());
    }

}
