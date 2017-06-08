package com.mulesoft.agent.eventtracker.cloudhub;

/**
 * Thrown when failed to store replay payload to S3.
 */
public class S3StorageException extends Exception
{

    private static final long serialVersionUID = 1L;

    public S3StorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
