package com.mulesoft.agent.monitoring.publisher.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MuleMessageSnapshotFactoryTest {

    MuleMessageSnapshotFactory factory = new MuleMessageSnapshotFactory();

    @Test
    public void testLastSnapshot() {
        MuleMessageSnapshotFactory.MuleMessageSnapshot snapshot = factory.newSnapshot(10L, 1487730487L);

        assertNotNull("Snapshot should be created", snapshot);
        assertEquals("Should match input", 10L, snapshot.messageCount);
        assertEquals("Should match input", 1487730487L, snapshot.timestamp);
    }
}
