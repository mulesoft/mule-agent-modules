package com.mulesoft.agent.eventtracker.cloudhub;

/**
 * Thrown when insights injection failed.
 */
public class InsightException extends Exception
{

    private static final long serialVersionUID = 1L;

    InsightException(String message)
    {
        super(message);
    }

    InsightException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
