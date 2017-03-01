package com.mulesoft.agent.monitoring.publisher.ingest.factory;

import com.google.common.collect.Lists;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.ingest.MetricClassificationTestCases;
import com.mulesoft.agent.monitoring.publisher.ingest.builder.IngestMetricBuilder;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricClassification;
import com.mulesoft.agent.monitoring.publisher.ingest.model.MetricSample;
import com.mulesoft.agent.monitoring.publisher.ingest.model.api.IngestMetric;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by svinci on 2/22/17.
 */
public class GarbageCollectionTimeMetricFactoryTest {

    private GarbageCollectionTimeMetricFactory instance = new GarbageCollectionTimeMetricFactory(new IngestMetricBuilder());

    @Test
    public void doesNotThrowNPEWhenGivenEmptyClassification() {
        MetricClassification testCase = new MetricClassification(Lists.<String>newLinkedList(), Lists.<Metric>newLinkedList());
        this.instance.apply(testCase, SupportedJMXBean.GC_MARK_SWEEP_TIME);
    }

    @Test
    public void doesNotThrowNPEWhenGivenNullClassification() {
        this.instance.apply(null, SupportedJMXBean.GC_MARK_SWEEP_TIME);
    }

    /**
     * https://www.mulesoft.org/jira/browse/CHHYBRID-2579
     */
    @Test
    public void doesNotThrowNPEWhenNotGivenMetrics() {
        MetricClassification testCase = new MetricClassification(
                Lists.newArrayList(SupportedJMXBean.JVM_UPTIME.getMetricName()),
                new MetricClassificationTestCases().completeTestCase(5d)
        );

        this.instance.apply(testCase, SupportedJMXBean.GC_MARK_SWEEP_TIME);
    }

    @Test
    public void supportsTheCorrectJMXBeans() {
        List<SupportedJMXBean> supportedMetrics = this.instance.getSupportedMetrics();
        Assert.assertEquals(2, supportedMetrics.size());
        Assert.assertTrue("Supported metrics didn't contain GC_MARK_SWEEP_TIME", supportedMetrics.contains(SupportedJMXBean.GC_MARK_SWEEP_TIME));
        Assert.assertTrue("Supported metrics didn't contain GC_PAR_NEW_TIME", supportedMetrics.contains(SupportedJMXBean.GC_PAR_NEW_TIME));
    }

    @Test
    public void doesCalculationsCorrectly() {

        MetricClassification firstTestCase = new MetricClassification(
                Lists.newArrayList(SupportedJMXBean.GC_PAR_NEW_TIME.getMetricName(), SupportedJMXBean.GC_MARK_SWEEP_TIME.getMetricName(), SupportedJMXBean.JVM_UPTIME.getMetricName()),
                new MetricClassificationTestCases().completeTestCase(5d)
        );

        List<IngestMetric> firstCaseSamples = Lists.newArrayList(
                this.instance.apply(firstTestCase, SupportedJMXBean.GC_MARK_SWEEP_TIME),
                this.instance.apply(firstTestCase, SupportedJMXBean.GC_PAR_NEW_TIME)
        );

        doAsserts(firstCaseSamples);

    }

    @Test
    public void doesCalculationsCorrectlyASecondTime() {
        MetricClassification secondTestCase = new MetricClassification(
                Lists.newArrayList(SupportedJMXBean.GC_PAR_NEW_TIME.getMetricName(), SupportedJMXBean.GC_MARK_SWEEP_TIME.getMetricName(), SupportedJMXBean.JVM_UPTIME.getMetricName()),
                new MetricClassificationTestCases().completeTestCase(6d)
        );

        List<IngestMetric> secondCaseSamples = Lists.newArrayList(
                this.instance.apply(secondTestCase, SupportedJMXBean.GC_MARK_SWEEP_TIME),
                this.instance.apply(secondTestCase, SupportedJMXBean.GC_PAR_NEW_TIME)
        );

        doAsserts(secondCaseSamples);
    }

    private void doAsserts(List<IngestMetric> samples) {
        for (IngestMetric sample : samples) {
            Assert.assertEquals(100d, sample.getAvg());
            Assert.assertEquals(10d, sample.getCount());
            Assert.assertEquals(100d, sample.getMax());
            Assert.assertEquals(100d, sample.getMin());
            Assert.assertEquals(100d * 10d, sample.getSum());
        }
    }

}
