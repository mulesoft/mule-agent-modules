package com.mulesoft.agent.monitoring.publisher.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MemorySnapshotFactoryTest {

    MemorySnapshotFactory factory = new MemorySnapshotFactory();

    @Test
    public void testLastSnapshot() {
        MemorySnapshotFactory.MemorySnapshot snapshot = factory.newSnapshot(10000L, 5000L, 1487730487L);

        assertNotNull("Snapshot should be created", snapshot);
        assertEquals("Should match input", 10000L, snapshot.getMemoryTotalMaxBytes());
        assertEquals("Should match input", 5000L, snapshot.getMemoryTotalUsedBytes());
        assertEquals("Should match input", 1487730487L, snapshot.getTimestamp());
        assertEquals("Should compute proper percentage", 50D, snapshot.getMemoryPercentUsed(), 1D);
    }
}
