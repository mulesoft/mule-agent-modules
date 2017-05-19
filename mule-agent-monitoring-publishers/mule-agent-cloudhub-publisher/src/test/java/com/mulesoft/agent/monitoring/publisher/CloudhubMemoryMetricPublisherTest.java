package com.mulesoft.agent.monitoring.publisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.mulesoft.agent.domain.monitoring.Metric;
import com.mulesoft.agent.domain.monitoring.SupportedJMXBean;
import com.mulesoft.agent.monitoring.publisher.client.DefaultCloudhubPlatformClient;
import com.mulesoft.agent.monitoring.publisher.factory.MemorySnapshotFactory;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CloudhubMemoryMetricPublisherTest {

    private DefaultCloudhubPlatformClient client = Mockito.mock(DefaultCloudhubPlatformClient.class);
    private CloudhubMemoryMetricPublisher publisher = new CloudhubMemoryMetricPublisher(client,
            new MemorySnapshotFactory());

    @Test
    public void testSendStats() {
        Mockito.doReturn(true)
               .when(client).sendMemoryStats(Mockito.any(MemorySnapshotFactory.MemorySnapshot.class));
        Collection<ArrayList<Metric>> input = setup();

        assertTrue("Flush should complete", publisher.flush(input));
        Mockito.verify(client).sendMemoryStats(Mockito.any(MemorySnapshotFactory.MemorySnapshot.class));
    }

    @Test
    public void testSendStatsException() {
        Collection<ArrayList<Metric>> input = setup();
        Mockito.doThrow(new RuntimeException())
               .when(client).sendMemoryStats(Mockito.any(MemorySnapshotFactory.MemorySnapshot.class));

        assertFalse("Flush should fail", publisher.flush(input));
    }

    private Collection<ArrayList<Metric>> setup() {
        Metric memoryMax = new Metric(1486668276960L, SupportedJMXBean.HEAP_TOTAL.getMetricName(), 10000L);
        Metric memoryUsed = new Metric(1486668276960L, SupportedJMXBean.HEAP_USAGE.getMetricName(), 1000L);

        Collection<ArrayList<Metric>> l = new ArrayList<>();
        l.add(new ArrayList<>(Arrays.asList(memoryMax, memoryUsed)));
        return l;
    }
}
