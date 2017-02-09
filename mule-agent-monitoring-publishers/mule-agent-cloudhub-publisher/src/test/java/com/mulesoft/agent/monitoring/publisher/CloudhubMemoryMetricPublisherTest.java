package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CloudhubMemoryMetricPublisherTest {

    @Test
    public void testPercentageCalculation() {
        CloudhubMemoryMetricPublisher.MemorySnapshot ms = new CloudhubMemoryMetricPublisher.MemorySnapshot(100L, 30L, 34343443L);
        assertEquals(30D, ms.memoryPercentUsed, 1D);
    }
}
