package com.mulesoft.agent.monitoring.publisher.factory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Creates and holds state of last MuleMessages metric snapshot.
 */
@Named
@Singleton
public class MuleMessageSnapshotFactory
{

    public MuleMessageSnapshot newSnapshot(long messageCount, long timestamp)
    {
        return new MuleMessageSnapshot(messageCount, timestamp);
    }

    /**
     * Holds state of last MuleMessages metric snapshot.
     */
    public static class MuleMessageSnapshot
    {
        public final long messageCount;
        public final long timestamp;

        MuleMessageSnapshot(long messageCount, long timestamp)
        {
            this.messageCount = messageCount;
            this.timestamp = timestamp;
        }
    }
}
