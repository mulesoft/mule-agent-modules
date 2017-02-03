package com.mulesoft.agent.monitoring.publisher.factory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Creates and holds state of last Memory metric snapshot.
 */
@Named
@Singleton
public class MemorySnapshotFactory {

    public MemorySnapshot newSnapshot(
            long memoryMax, long memoryUsed, long timestamp) {
        double memoryPercent = ((double) memoryUsed / memoryMax) * 100D;
        return new MemorySnapshot(memoryMax, memoryUsed, memoryPercent, timestamp);
    }


    public static class MemorySnapshot {
        public final long memoryTotalMaxBytes;
        public final long memoryTotalUsedBytes;
        public final double memoryPercentUsed;
        public final long timestamp;

        MemorySnapshot(long memoryTotalMaxBytes, long memoryTotalUsedBytes,
                       double memoryPercentUsed, long timestamp) {
            this.memoryTotalMaxBytes = memoryTotalMaxBytes;
            this.memoryTotalUsedBytes = memoryTotalUsedBytes;
            this.memoryPercentUsed = memoryPercentUsed;
            this.timestamp = timestamp;
        }
    }
}
