package com.mulesoft.agent.monitoring.publisher.factory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Creates and holds state of last Memory metric snapshot.
 */
@Named
@Singleton
public class MemorySnapshotFactory
{

    private static final double PERCENTAGE_CONVERSION = 100D;

    public MemorySnapshot newSnapshot(long memoryMax, long memoryUsed, long timestamp)
    {
        double memoryPercent = ((double) memoryUsed / memoryMax) * PERCENTAGE_CONVERSION;
        return new MemorySnapshot(memoryMax, memoryUsed, memoryPercent, timestamp);
    }

    /**
     * Holds state of last Memory metric snapshot.
     */
    public static class MemorySnapshot
    {
        private final long memoryTotalMaxBytes;
        private final long memoryTotalUsedBytes;
        private final double memoryPercentUsed;
        private final long timestamp;

        MemorySnapshot(long memoryTotalMaxBytes, long memoryTotalUsedBytes, double memoryPercentUsed, long timestamp)
        {
            this.memoryTotalMaxBytes = memoryTotalMaxBytes;
            this.memoryTotalUsedBytes = memoryTotalUsedBytes;
            this.memoryPercentUsed = memoryPercentUsed;
            this.timestamp = timestamp;
        }

        public long getMemoryTotalMaxBytes()
        {
            return memoryTotalMaxBytes;
        }

        public long getMemoryTotalUsedBytes()
        {
            return memoryTotalUsedBytes;
        }

        public double getMemoryPercentUsed()
        {
            return memoryPercentUsed;
        }

        public long getTimestamp()
        {
            return timestamp;
        }
    }
}
