package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.mulesoft.agent.common.internalhandler.cloudhub.CloudhubPlatformClient;
import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;

public class CloudhubMemoryMetricPublisherTest {

    private CloudhubPlatformClient client = mock(CloudhubPlatformClient.class);
    private CloudhubMemoryMetricPublisher publisher = new CloudhubMemoryMetricPublisher(client);
    private Collection<List<Metric>> input;

    @Test
    public void testPercentageCalculation() {
        CloudhubMemoryMetricPublisher.MemorySnapshot ms = new CloudhubMemoryMetricPublisher.MemorySnapshot(100L, 30L,
                34343443L);
        assertEquals("Should compute proper percentage", 30D, ms.memoryPercentUsed, 1D);
    }

    @Test
    public void testGenerateSnapshot() {
        setup();
        doAnswer(invocation -> null)
                .when(client).sendMemoryStats(any());
        publisher.flush(input);

        assertNotNull("Snapshot should be created", publisher.getLastSnapshot());
        assertTrue("Should match input", publisher.getLastSnapshot().memoryTotalMaxBytes == 10000L);
        assertTrue("Should match input", publisher.getLastSnapshot().memoryTotalUsedBytes == 1000L);
        assertTrue("Should match input", publisher.getLastSnapshot().timestamp == 1486668276960L);
    }

    private void setup() {
        Metric memoryMax = new Metric(1486668276960L, SupportedJMXBean.HEAP_TOTAL.getMetricName(), new Number() {

            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public long longValue() {
                return 10000L;
            }

            @Override
            public float floatValue() {
                return 0;
            }

            @Override
            public double doubleValue() {
                return 0;
            }
        });

        Metric memoryUsed = new Metric(1486668276960L, SupportedJMXBean.HEAP_USAGE.getMetricName(), new Number() {

            @Override
            public int intValue() {
                return 0;
            }

            @Override
            public long longValue() {
                return 1000L;
            }

            @Override
            public float floatValue() {
                return 0;
            }

            @Override
            public double doubleValue() {
                return 0;
            }
        });

        Collection<List<Metric>> l = new ArrayList<>();
        l.add(new ArrayList<>(Arrays.asList(memoryMax, memoryUsed)));
        input = l;
    }
}
