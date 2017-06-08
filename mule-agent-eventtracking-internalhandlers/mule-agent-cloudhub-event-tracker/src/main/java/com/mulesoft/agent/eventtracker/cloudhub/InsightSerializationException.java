package com.mulesoft.agent.eventtracker.cloudhub;

/**
 * Thrown when failed to serialize insight events.
 */
public class InsightSerializationException extends InsightException
{

    private static final long serialVersionUID = 1L;

    public InsightSerializationException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
