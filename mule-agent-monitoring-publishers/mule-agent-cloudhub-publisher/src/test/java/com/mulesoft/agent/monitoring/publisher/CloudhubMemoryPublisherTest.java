package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.services.OnOffSwitch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CloudhubMemoryPublisherTest {

    @Test
    public void testPercentageCalculation() {
        CloudhubMemoryPublisher.MemorySnapshot ms = new CloudhubMemoryPublisher.MemorySnapshot(100L, 30L, 34343443L);
        assertEquals(30D, ms.memoryPercentUsed, 1D);
    }

    @Test
    public void testMemorySnapshot() {
        OnOffSwitch enabledSwitch = mock(OnOffSwitch.class);
        when(enabledSwitch.isEnabled()).thenReturn(true);
        CloudhubMemoryPublisher publisher = new CloudhubMemoryPublisher(enabledSwitch);

        Metric metricHeapTotal = mock(Metric.class);
        when(metricHeapTotal.getName()).thenReturn(SupportedJMXBean.HEAP_TOTAL.getMetricName());
        when(metricHeapTotal.getValue()).thenReturn(metricNumber);
        when(metricHeapTotal.getTimestamp()).thenReturn(1486420200815L);
        Metric metricHeapUsage = mock(Metric.class);
        when(metricHeapUsage.getName()).thenReturn(SupportedJMXBean.HEAP_USAGE.getMetricName());
        when(metricHeapUsage.getValue()).thenReturn(metricNumber);

        List<Metric> metrics = new ArrayList<>(Arrays.asList(metricHeapTotal, metricHeapUsage));
        boolean result = publisher.flush(Collections.singletonList(metrics));

        assertTrue("Publisher flush should be successful", result);
        assertTrue(publisher.lastSnapshot.timestamp > 0L);
        assertTrue(publisher.lastSnapshot.memoryTotalMaxBytes > 0L);
        assertTrue(publisher.lastSnapshot.memoryTotalUsedBytes > 0L);
    }


    private Number metricNumber = new Number() {
        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 100;
        }

        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public double doubleValue() {
            return 0;
        }
    };
}
