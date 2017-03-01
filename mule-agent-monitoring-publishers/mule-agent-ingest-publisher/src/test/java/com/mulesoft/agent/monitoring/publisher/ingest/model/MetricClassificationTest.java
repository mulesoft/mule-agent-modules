package com.mulesoft.agent.monitoring.publisher.ingest.model;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.MetricClassificationTestCases;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class MetricClassificationTest
{

    private MetricClassificationTestCases metricClassificationTestCases = new MetricClassificationTestCases();

    private List<String> keys = Lists.newLinkedList();
    private List<String> keysWithNull = Lists.newLinkedList();

    @Before
    public void setUp() {
        SupportedJMXBean[] supportedJMXBeans = SupportedJMXBean.values();

        for (SupportedJMXBean bean : supportedJMXBeans) {
            keys.add(bean.getMetricName());
            keysWithNull.add(bean.getMetricName());
        }
        keysWithNull.add(null);
    }

    @Test
    public void shouldNotThrowNPEWhenIPassACoupleNullsToIt()
    {
        new MetricClassification(keys, metricClassificationTestCases.someNullsTestCase());
    }

    @Test
    public void shouldNotThrowNPEWhenIPassNullToIt()
    {
        new MetricClassification(null, null);
    }

    @Test
    public void shouldNotThrowNPEWhenKeysIsNull()
    {
        new MetricClassification(null, metricClassificationTestCases.completeTestCase(5d));
    }

    @Test
    public void shouldNotThrowNPEWhenMetricsIsNull()
    {
        new MetricClassification(keys, null);
    }

    @Test
    public void shouldNotThrowNPEWhenKeysContainsNull()
    {
        new MetricClassification(keysWithNull, metricClassificationTestCases.completeTestCase(5d));
    }

    @Test
    public void shouldBeEmptyWhenKeysIsEmpty()
    {
        MetricClassification classification = new MetricClassification(Lists.<String>newLinkedList(), metricClassificationTestCases.completeTestCase(5d));
        Map<String, List<Metric>> map = classification.getClassification();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void shouldBeEmptyWhenCollectionOfEmptyListsIsGiven()
    {
        MetricClassification classification = new MetricClassification(this.keys, metricClassificationTestCases.emptyList());
        Map<String, List<Metric>> map = classification.getClassification();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void happyCase()
    {
        MetricClassification classification = new MetricClassification(this.keys, metricClassificationTestCases.completeTestCase(5d));

        SupportedJMXBean[] supportedJMXBeans = SupportedJMXBean.values();

        for (SupportedJMXBean bean : supportedJMXBeans) {
            int actualMetricQty = classification.getMetrics(bean.getMetricName()).size();
            String assertionMessage = "Expected 10 metrics for bean " + bean.name() + ", but found " + actualMetricQty;
            Assert.assertEquals(assertionMessage, 10, actualMetricQty);
        }
    }

}
