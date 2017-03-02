package com.mulesoft.agent.monitoring.publisher.client;

import com.mulesoft.agent.monitoring.publisher.factory.MemorySnapshotFactory.MemorySnapshot;
import com.mulesoft.agent.monitoring.publisher.factory.MuleMessageSnapshotFactory.MuleMessageSnapshot;

/**
 * HTTP client for talking to Cloudhub Platform services.
 */
public interface CloudhubPlatformClient {

    /**
     * Triggers a POST that sends over memory statistics.
     *
     * @param snapshot the POJO entity
     * @return true if sending snapshot was successful, else false
     */
    boolean sendMemoryStats(MemorySnapshot snapshot);

    /**
     * Triggers a POST that sends over Mule messages statistics.
     *
     * @param snapshot the POJO entity
     * @return true if sending snapshot was successful, else false
     */
    boolean sendMessagesStats(MuleMessageSnapshot snapshot);
}
