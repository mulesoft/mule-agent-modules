package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.client.DefaultCloudhubPlatformClient;
import com.mulesoft.agent.monitoring.publisher.factory.MemorySnapshotFactory;

public class CloudhubMemoryMetricPublisherTest {

    private DefaultCloudhubPlatformClient client = Mockito.mock(DefaultCloudhubPlatformClient.class);
    private CloudhubMemoryMetricPublisher publisher = new CloudhubMemoryMetricPublisher(client,
            new MemorySnapshotFactory());

    @Test
    public void testSendStats() {
        Mockito.doReturn(true)
               .when(client).sendMemoryStats(Mockito.any());
        Collection<List<Metric>> input = setup();

        assertTrue("Flush should complete", publisher.flush(input));
        Mockito.verify(client).sendMemoryStats(Mockito.any());
    }

    @Test
    public void testSendStatsException() {
        Collection<List<Metric>> input = setup();
        Mockito.doThrow(new RuntimeException())
               .when(client).sendMemoryStats(Mockito.any());

        assertFalse("Flush should fail", publisher.flush(input));
    }

    private Collection<List<Metric>> setup() {
        Metric memoryMax = new Metric(1486668276960L, SupportedJMXBean.HEAP_TOTAL.getMetricName(), 10000L);
        Metric memoryUsed = new Metric(1486668276960L, SupportedJMXBean.HEAP_USAGE.getMetricName(), 1000L);

        Collection<List<Metric>> l = new ArrayList<>();
        l.add(new ArrayList<>(Arrays.asList(memoryMax, memoryUsed)));
        return l;
    }
}
