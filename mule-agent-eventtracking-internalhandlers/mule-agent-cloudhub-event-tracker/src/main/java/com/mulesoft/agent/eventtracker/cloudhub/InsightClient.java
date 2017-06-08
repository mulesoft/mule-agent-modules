package com.mulesoft.agent.eventtracker.cloudhub;

import java.util.Collection;

/**
 * Cloudhub Platform client for insight.
 */
public interface InsightClient
{

    /**
     * Serialize the collection of InsightEvent and send over to Cloudhub Platform.
     *
     * @param events
     *            to be sent to Cloudhub Platform
     * @throws InsightSerializationException
     *             if Cloudhub Platform is unreachable or response status is not OK
     * @throws InsightSerializationException
     *             if any InsightEvent is not serializable
     */
    void sendInsight(Collection<InsightEvent> events) throws InsightException;
}
