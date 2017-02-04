package com.mulesoft.agent.monitoring.publisher;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CloudhubMemoryPublisherTest {

    @Test
    public void testPercentageCalculation() {
        CloudhubMemoryPublisher.MemorySnapshot ms = new CloudhubMemoryPublisher.MemorySnapshot(100L, 30L, 34343443L);
        assertEquals(30D, ms.getMemoryPercentUsed(), 1D);
    }

    @Test
    public void testMemorySnapshot() {

    }
}
