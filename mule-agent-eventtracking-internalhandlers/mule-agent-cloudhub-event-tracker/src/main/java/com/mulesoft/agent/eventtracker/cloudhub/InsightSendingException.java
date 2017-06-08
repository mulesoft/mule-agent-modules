package com.mulesoft.agent.eventtracker.cloudhub;

/**
 * Thrown when failed to send insight events to Cloudhub Platform.
 */
public class InsightSendingException extends InsightException
{

    private static final long serialVersionUID = 1L;

    public InsightSendingException(String message)
    {
        super(message);
    }

    public InsightSendingException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
